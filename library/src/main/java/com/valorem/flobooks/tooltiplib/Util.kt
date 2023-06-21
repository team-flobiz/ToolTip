package com.valorem.flobooks.tooltiplib

import android.content.res.Resources
import android.view.View
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* convert dp value to pixels */
val Int.dp: Float
    get() = this.times(Resources.getSystem().displayMetrics.density)

/* convert sp value to pixels */
val Int.sp: Float
    get() = this.times(Resources.getSystem().displayMetrics.scaledDensity)

/* Add safe runnable to block execution on View queue as lifecycle aware block */
fun <T : View> T.safeRun(
    delayInMs: Long = 0L,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: T.() -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(delayInMs)
        block()
    }
}