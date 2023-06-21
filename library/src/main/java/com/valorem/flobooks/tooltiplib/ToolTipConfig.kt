package com.valorem.flobooks.tooltiplib

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Px

/* tooltip config - expose if required but mostly static config */
internal data class ToolTipConfig(
    val defaultGravity: ToolTipGravity = ToolTipGravity.Top,
    @Px val arrowHeadHeight: Float = 6.dp,
    @Px val cornerRadius: Float = 4.dp,
    @Px val edgePadding: Float = 16.dp,
    /* horizontal to vertical */
    @Px val textPadding: Pair<Float, Float> = 10.dp to 6.dp,
    @Px val anchorPadding: Float = 6.dp,
    val animationDuration: Long = 150,
    @ColorInt val backgroundColor: Int = Color.parseColor("#353452"),
    @Px val textSize: Float = 13.sp,
    @ColorInt val textColor: Int = Color.WHITE,
    /* width multiplexer to adjust width within bound */
    val widthMultiplexer: Float = 0.75f,
)