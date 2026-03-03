package com.example.engineeringcalculator.util

import kotlin.math.ln
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun `ln of truncated e formats to one`() {
        val value = ln(2.7182818285)
        assertEquals("1", NumberFormatter.format(value))
    }

    @Test
    fun `small residual formats to zero`() {
        assertEquals("0", NumberFormatter.format(-2.06823935e-11))
    }
}
