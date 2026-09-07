package com.aos.core.data.search

import com.aos.core.common.util.MathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun testBasicCalculations() {
        assertEquals("120", MathEvaluator.evaluate("15 * 8"))
        assertEquals("1900", MathEvaluator.evaluate("1500 + 400"))
        assertEquals("25", MathEvaluator.evaluate("100 / 4"))
        assertEquals("50", MathEvaluator.evaluate("25 * 2"))
        assertEquals("60", MathEvaluator.evaluate("(10 + 10) * 3"))
    }

    @Test
    fun testAdvancedMath() {
        assertEquals("12", MathEvaluator.evaluate("sqrt(144)"))
        assertEquals("8", MathEvaluator.evaluate("2 ^ 3"))
    }

    @Test
    fun testNonMathStrings() {
        assertNull(MathEvaluator.evaluate("WhatsApp"))
        assertNull(MathEvaluator.evaluate("Instagram"))
        assertNull(MathEvaluator.evaluate("a"))
        assertNull(MathEvaluator.evaluate("hello world"))
    }
}
