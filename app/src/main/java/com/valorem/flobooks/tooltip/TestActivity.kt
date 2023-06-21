package com.valorem.flobooks.tooltip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.valorem.flobooks.tooltiplib.ToolTip
import com.valorem.flobooks.tooltiplib.ToolTipDuration

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        listOf(R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5)
            .map { findViewById<AppCompatButton>(it) }
            .onEach {
                it.setOnClickListener { view -> showToolTip(view) }
            }
    }

    private fun showToolTip(anchor: View) {
        ToolTip.Builder(anchor)
            .setText("This is simple tooltip on view: $anchor")
            .show(this, dismissDuration = ToolTipDuration.Medium)
    }
}