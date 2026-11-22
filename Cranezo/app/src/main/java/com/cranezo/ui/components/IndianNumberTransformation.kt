package com.cranezo.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shows an amount with commas as it is typed, in the Indian way: 125000 -> 1,25,000 and 1234567.5 -> 12,34,567.5.
 * The typed value itself stays plain digits (and one decimal point), so nothing else has to change.
 */
object IndianNumberTransformation : VisualTransformation {

    /** True when a comma stands in front of the digit at [index] of an integer part that is [length] digits long */
    private fun commaBefore(index: Int, length: Int): Boolean {
        if (index <= 0) return false
        val fromRight = length - index
        return fromRight >= 3 && (fromRight - 3) % 2 == 0
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val dot = raw.indexOf('.')
        val intLength = if (dot >= 0) dot else raw.length

        val out = StringBuilder()
        var commas = 0
        for (i in raw.indices) {
            if (i < intLength && commaBefore(i, intLength)) {
                out.append(',')
                commas++
            }
            out.append(raw[i])
        }

        val mapping = object : OffsetMapping {
            // how many commas stand before the cursor when it is at [offset] of the typed text
            private fun commasBefore(offset: Int): Int =
                (1..minOf(offset, intLength - 1).coerceAtLeast(0)).count { commaBefore(it, intLength) }

            override fun originalToTransformed(offset: Int): Int = offset + commasBefore(offset)

            override fun transformedToOriginal(offset: Int): Int {
                for (o in 0..raw.length) {
                    if (o + commasBefore(o) >= offset) return o
                }
                return raw.length
            }
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}
