package com.example.engineeringcalculator.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

object NumberFormatter {

    private const val SCALE = 12
    private const val NEAR_ZERO_EPS = 1e-12
    private const val SCI_UPPER_THRESHOLD = 1e12
    private const val SCI_LOWER_THRESHOLD = 1e-6
    private const val MAX_PLAIN_LENGTH = 16

    fun format(value: Double): String {
        if (abs(value) < NEAR_ZERO_EPS) return "0"

        val absValue = abs(value)
        if (absValue >= SCI_UPPER_THRESHOLD || absValue < SCI_LOWER_THRESHOLD) {
            return toScientific(value)
        }

        val rounded = BigDecimal.valueOf(value)
            .setScale(SCALE, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val text = rounded.toPlainString()
        if (text == "-0") return "0"
        if (text.length > MAX_PLAIN_LENGTH) return toScientific(value)
        return text
    }

    private fun toScientific(value: Double): String {
        val scientific = String.format(Locale.US, "%.10e", value)
        val parts = scientific.split('e')
        if (parts.size != 2) return scientific

        val mantissa = parts[0]
            .trimEnd('0')
            .trimEnd('.')
        val exponent = parts[1]
            .replace("+0", "+")
            .replace("-0", "-")

        return "$mantissa" + "e$exponent"
    }
}
