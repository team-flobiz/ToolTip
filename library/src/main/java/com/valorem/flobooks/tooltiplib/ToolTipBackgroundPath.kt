package com.valorem.flobooks.tooltiplib

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.Px

/* Tooltip background path boiler plate */
internal class ToolTipBackgroundPath(
    boundRect: RectF,
    anchorRect: RectF,
    gravity: ToolTipGravity,
    @Px arrowHeadHeight: Float,
    @Px cornerRadius: Float
) : Path() {

    init {
        /* move to left-top*/
        moveTo(boundRect.left + cornerRadius, boundRect.top)
        /* top arrow*/
        gravity.takeIf { it == ToolTipGravity.Bottom }
            ?.also { _ ->
                lineTo(anchorRect.centerX() - arrowHeadHeight, boundRect.top)
                lineTo(anchorRect.centerX(), boundRect.top - arrowHeadHeight)
                lineTo(anchorRect.centerX() + arrowHeadHeight, boundRect.top)
            }
        /* to right-top*/
        lineTo(boundRect.right - cornerRadius, boundRect.top)
        quadTo(boundRect.right, boundRect.top, boundRect.right, boundRect.top + cornerRadius)
        /* line to right-bottom*/
        lineTo(boundRect.right, boundRect.bottom - cornerRadius)
        quadTo(boundRect.right, boundRect.bottom, boundRect.right - cornerRadius, boundRect.bottom)
        /* bottom arrow*/
        gravity.takeIf { it == ToolTipGravity.Top }
            ?.also { _ ->
                lineTo(anchorRect.centerX() + arrowHeadHeight, boundRect.bottom)
                lineTo(anchorRect.centerX(), boundRect.bottom + arrowHeadHeight)
                lineTo(anchorRect.centerX() - arrowHeadHeight, boundRect.bottom)
            }
        /* line to left-bottom*/
        lineTo(boundRect.left + cornerRadius, boundRect.bottom)
        quadTo(boundRect.left, boundRect.bottom, boundRect.left, boundRect.bottom - cornerRadius)
        /* line to left-top*/
        lineTo(boundRect.left, boundRect.top + cornerRadius)
        quadTo(boundRect.left, boundRect.top, boundRect.left + cornerRadius, boundRect.top)
    }
}