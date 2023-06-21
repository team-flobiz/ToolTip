package com.valorem.flobooks.tooltiplib


/**
 * The duration of the tooltip refers to the length of time before it disappears after being displayed.
 */
enum class ToolTipDuration(val delay: Int) {
    Tiny(1000),
    Short(2000),
    Medium(5000),
    Long(10000)
}