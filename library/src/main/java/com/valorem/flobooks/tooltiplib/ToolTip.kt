package com.valorem.flobooks.tooltiplib

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.toRectF
import androidx.core.graphics.withScale
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.coroutines.Job

@SuppressLint("ViewConstructor")
class ToolTip private constructor(
    private val anchorView: View,
    private val text: String,
    private val config: ToolTipConfig
) : FrameLayout(anchorView.context, null, -1) {

    init {
        setWillNotDraw(false)
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
    }

    /* paint */
    private val textPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = config.textColor
                textSize = config.textSize
                textAlign = Paint.Align.LEFT
            }
    }
    private val backgroundPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
            .apply {
                color = config.backgroundColor
                style = Paint.Style.FILL
            }
    }

    /* tooltip text layout for measuring required text size */
    private val textLayout by lazy {
        ToolTipTextLayout(
            text = text,
            maxWidth = (width - 2 * config.run { edgePadding + textPadding.first }).times(config.widthMultiplexer),
            textPaint = textPaint
        )
    }

    /* anchor bound rect */
    private val anchorRect: Rect by lazy {
        anchorView.let { anchor ->
            intArrayOf(-1, -1)
                .also { anchor.getLocationInWindow(it) }
                .run { Rect(get(0), get(1), get(0) + anchor.width, get(1) + anchor.height) }
        }
    }

    /* predicted tooltip gravity */
    private val gravity by lazy {
        val requiredHeight = textLayout.heightRequired
            .plus(2 * config.textPadding.second)
            .plus(config.arrowHeadHeight)
            .plus(config.anchorPadding)

        /* recalculate gravity for available space from default gravity */
        when {
            /* top overshooting */
            config.run {
                defaultGravity == ToolTipGravity.Top
                        && (anchorRect.top - requiredHeight) < edgePadding
            } -> ToolTipGravity.Bottom

            /* bottom overshooting */
            config.run {
                defaultGravity == ToolTipGravity.Bottom && (anchorRect.bottom + requiredHeight) > (height - edgePadding)
            } -> ToolTipGravity.Top

            /* assume default */
            else -> config.defaultGravity
        }
    }

    /* create background bound rect */
    private val backgroundRect by lazy {
        textLayout
            .run {
                widthRequired.plus(2 * config.textPadding.first) to
                        heightRequired.plus(2 * config.textPadding.second)
            }
            /* required background size -> first : width / second : height */
            .run {
                /* calculate left / start-x */
                val _left = (anchorRect.centerX() - (first / 2))
                    .coerceIn(config.edgePadding, width - first - config.edgePadding)

                /* calculate top / start-y */
                val _top = when (gravity) {
                    ToolTipGravity.Top -> anchorRect.top - second - config.anchorPadding - config.arrowHeadHeight
                    ToolTipGravity.Bottom -> anchorRect.bottom + config.anchorPadding + config.arrowHeadHeight
                }

                RectF(_left, _top, _left + first, _top + second)
            }
    }

    /* create tool */
    private val textRect by lazy {
        RectF(backgroundRect).apply { inset(config.textPadding.first, config.textPadding.second) }
    }

    /* generate background path */
    private val backgroundPath by lazy {
        ToolTipBackgroundPath(
            boundRect = backgroundRect,
            anchorRect = anchorRect.toRectF(),
            gravity = gravity,
            arrowHeadHeight = config.arrowHeadHeight,
            cornerRadius = config.cornerRadius
        )
    }

    /* current animator factor */
    private var animatorFactor = 0f
        set(value) {
            field = value
            invalidate()
        }

    /* show animator from last animator factor */
    private val showAnimator by lazy {
        ValueAnimator.ofFloat(animatorFactor, 1f)
            .apply {
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener { animatorFactor = it.animatedValue as Float }
            }
            .setDuration(config.animationDuration)
    }

    /* hide animator from last animator factor */
    private val hideAnimator by lazy {
        ValueAnimator.ofFloat(animatorFactor, 0f)
            .apply {
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener { animatorFactor = it.animatedValue as Float }
                /* remove view from parent on animation ends */
                doOnEnd { parent?.run { this as? ViewGroup }?.removeView(this@ToolTip) }
                /* cancel if running */
                doOnStart { showAnimator.takeIf { it.isRunning }?.cancel() }
            }
            .setDuration(config.animationDuration)
    }

    private var autoDismissJob : Job? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        /* scale to animate from arrow head tip as pivot */
        canvas.withScale(
            x = animatorFactor,
            y = animatorFactor,
            pivotX = anchorRect.centerX().toFloat(),
            pivotY = when (gravity) {
                ToolTipGravity.Top -> backgroundRect.bottom + config.arrowHeadHeight
                ToolTipGravity.Bottom -> backgroundRect.top - config.arrowHeadHeight
            }.toFloat()
        )
        {
            /* draw background */
            drawPath(backgroundPath, backgroundPaint)
            /* draw text on top */
            textLayout.lines
                .onEachIndexed { index, textLine ->
                    drawText(
                        textLine,
                        textRect.left,
                        textRect.top + (index * textLayout.lineHeight) - textPaint.ascent(),
                        textPaint
                    )
                }
        }
    }

    /* intercept touch to dismiss tooltip */
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        dismiss()
        return true
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
            dismiss()
        return true
    }

    /* dismiss tooltip */
    fun dismiss() {
        autoDismissJob?.takeIf { it.isActive }?.cancel()
        hideAnimator.start()
    }

    /* show tooltip */
    private fun show(activity: Activity, autoDismissDuration: ToolTipDuration? = null) {
        (activity.window.decorView as ViewGroup?)?.addView(this)
        showAnimator.start()
        requestFocus()

        autoDismissDuration?.delay?.toLong()?.takeIf { it > 0 }?.let { duration ->
            autoDismissJob = anchorView.safeRun(duration) {
                dismiss()
            }
        }
    }

    /* tooltip - builder */
    class Builder(private val anchorView: View) {
        /* expose config via build as required */
        private var config = ToolTipConfig()

        /* required lateinit vars */
        private lateinit var text: String

        fun setText(text: String) = apply { this@Builder.text = text }

        /* default as - it will be recalculated based on available space on UI */
        fun setDefaultGravity(gravity: ToolTipGravity) =
            apply { config = config.copy(defaultGravity = gravity) }

        /* validate and create tooltip instance */
        fun create(): ToolTip =
            when {
                ::text.isInitialized.not() ->
                    throw IllegalArgumentException("setText(TextResource) to Builder before creating ToolTip")

                else -> ToolTip(anchorView, text, config)
            }

        /* show tooltip and return instance */
        fun show(activity: Activity, dismissDuration : ToolTipDuration? = null): ToolTip = create().apply { show(activity, dismissDuration) }
    }


}