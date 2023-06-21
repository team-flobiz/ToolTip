package com.valorem.flobooks.tooltiplib

import android.text.TextPaint

/* tooltip text layout based on @param textPaint bound within @maxWidth */
internal class ToolTipTextLayout(text: String, maxWidth: Float, textPaint: TextPaint) {
    private val delimiter = '\n'

    /* required line height */
    val lineHeight by lazy { textPaint.run { descent() - ascent() } }

    /* required width for text bound */
    val widthRequired: Float by lazy { textPaint.measureText(text).takeIf { it <= maxWidth } ?: maxWidth }

    /* text lines split for max width */
    val lines by lazy {
        buildString {
            /* split to words, based on spaces */
            text.split(' ')
                .forEach { word ->
                    when {
                        /* cannot accomodate word in line, wrap it */
                        textPaint.measureText(word) >= widthRequired -> {
                            word.forEach { character ->
                                when {
                                    /* cannot accomodate characted in current line, wrap it */
                                    substring(lastIndexOf(delimiter).takeIf { it >= 0 } ?: 0, length)
                                        .let { "$it$character" }
                                        .run(textPaint::measureText) >= widthRequired -> append(delimiter).append(character)

                                    /* append word */
                                    else -> append(character)
                                }
                            }
                            /* space after word */
                            append(' ')
                        }

                        /* cannot accomodate word in current line, move to next line */
                        substring(lastIndexOf(delimiter).takeIf { it >= 0 } ?: 0, length)
                            .let { "$it$word" }
                            .run(textPaint::measureText) > widthRequired -> append(delimiter).append(word).append(' ')

                        /* append word */
                        else -> append(word).append(' ')
                    }
                }
        }.split(delimiter)
    }

    /* required height for text bound */
    val heightRequired: Float by lazy { lines.size * lineHeight}
}