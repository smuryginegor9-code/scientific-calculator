package com.example.engineeringcalculator.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun `2 plus 2 equals 4`() {
        assertAlmostEquals(4.0, engine.evaluate("2+2", AngleUnit.DEG))
    }

    @Test
    fun `parentheses priority works`() {
        assertAlmostEquals(20.0, engine.evaluate("(2+3)*4", AngleUnit.DEG))
    }

    @Test
    fun `sin 90 in degrees equals 1`() {
        assertAlmostEquals(1.0, engine.evaluate("sin(90)", AngleUnit.DEG))
    }

    @Test
    fun `ln of e equals 1`() {
        assertAlmostEquals(1.0, engine.evaluate("ln(e)", AngleUnit.RAD))
    }

    @Test
    fun `log10 100 equals 2`() {
        assertAlmostEquals(2.0, engine.evaluate("log10(100)", AngleUnit.RAD))
    }

    @Test
    fun `factorial of 5 equals 120`() {
        assertAlmostEquals(120.0, engine.evaluate("5!", AngleUnit.RAD))
    }

    @Test
    fun `root 27 of degree 3 equals 3`() {
        assertAlmostEquals(3.0, engine.evaluate("27^(1/3)", AngleUnit.RAD))
    }

    private fun assertAlmostEquals(expected: Double, actual: Double) {
        assertEquals(expected, actual, EPS)
    }

    private companion object {
        private const val EPS = 1e-10
    }
}
