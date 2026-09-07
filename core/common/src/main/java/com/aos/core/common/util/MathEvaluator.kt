package com.aos.core.common.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object MathEvaluator {

    private val numberFormat = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.getDefault()))

    /**
     * Attempts to evaluate [raw] as a mathematical expression.
     * Returns a formatted result string (e.g. "120" or "3.14159") if valid, or null if not an expression.
     */
    fun evaluate(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.length < 2) return null

        // Must contain at least one math operator or known function
        val hasMathOperator = trimmed.any { it in "+-*/÷xX%^" } ||
                trimmed.contains("sqrt", ignoreCase = true) ||
                trimmed.contains("abs", ignoreCase = true) ||
                trimmed.contains("sin", ignoreCase = true) ||
                trimmed.contains("cos", ignoreCase = true)

        if (!hasMathOperator) return null

        // Quick sanitation: replace display characters
        val sanitized = trimmed
            .replace("x", "*", ignoreCase = true)
            .replace("X", "*")
            .replace("÷", "/")
            .replace(",", ".")

        return try {
            val parser = SimpleParser(sanitized)
            val result = parser.parse()
            if (result.isInfinite() || result.isNaN()) {
                null
            } else {
                if (result % 1.0 == 0.0 && abs(result) < 1e12) {
                    result.toLong().toString()
                } else {
                    numberFormat.format(result)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private class SimpleParser(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            while (ch == ' ') nextChar()
            if (pos < str.length && ch != '\u0000') {
                throw IllegalArgumentException("Unexpected: $ch")
            }
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> {
                        val mod = parseFactor()
                        x %= mod
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            while (ch == ' ') nextChar()
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos

            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = str.substring(startPos, pos)
                x = numStr.toDouble()
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                val func = str.substring(startPos, pos).lowercase()
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "abs" -> abs(x)
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
            } else {
                throw IllegalArgumentException("Unexpected char: $ch")
            }

            if (eat('^')) x = x.pow(parseFactor())

            return x
        }
    }
}
