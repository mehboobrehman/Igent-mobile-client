package com.igent.mobile.domain.engine

import kotlin.math.pow

object Calculators {
    fun futureValue(principal: Double, ratePct: Double, years: Double, monthly: Double = 0.0): Double {
        val r = ratePct / 100.0 / 12.0
        val n = Math.round(years * 12.0)
        val growth = (1.0 + r).pow(n.toDouble())
        val annuity = if (r == 0.0) n.toDouble() else (growth - 1.0) / r
        return principal * growth + monthly * annuity
    }

    fun monthlySavingFor(target: Double, ratePct: Double, years: Double, start: Double = 0.0): Double {
        val r = ratePct / 100.0 / 12.0
        val n = Math.round(years * 12.0)
        val remaining = target - start * (1.0 + r).pow(n.toDouble())
        if (remaining <= 0.0) return 0.0
        return if (r == 0.0) remaining / n.toDouble() else remaining * r / ((1.0 + r).pow(n.toDouble()) - 1.0)
    }

    fun realReturnPct(nominalPct: Double, inflationPct: Double): Double {
        return ((1.0 + nominalPct / 100.0) / (1.0 + inflationPct / 100.0) - 1.0) * 100.0
    }

    fun cagrPct(start: Double, end: Double, years: Double): Double {
        if (start <= 0.0 || years <= 0.0) return 0.0
        return ((end / start).pow(1.0 / years) - 1.0) * 100.0
    }

    fun evaluate(expr: String): Double {
        val src = expr.replace(",", "").replace(Regex("\\s+"), "")
        if (!Regex("^[\\d.+\\-*/^()%eE]+$").matches(src)) {
            throw IllegalArgumentException("Only numbers, + - * / ^ % and parentheses are allowed.")
        }
        var i = 0
        fun peek(): Char = if (i < src.length) src[i] else '\u0000'

        fun number(): Double {
            val sub = src.substring(i)
            val m = Regex("^\\d*\\.?\\d+(?:[eE][+-]?\\d+)?").find(sub)
                ?: throw IllegalArgumentException("Expected a number at position ${i + 1}.")
            i += m.value.length
            return m.value.toDouble()
        }

        lateinit var parseSum: () -> Double

        fun factor(): Double {
            if (peek() == '-') {
                i++
                return -factor()
            }
            if (peek() == '+') {
                i++
                return factor()
            }
            val v: Double
            if (peek() == '(') {
                i++
                v = parseSum()
                if (peek() != ')') throw IllegalArgumentException("Missing closing parenthesis.")
                i++
            } else {
                v = number()
            }
            if (peek() == '%') {
                i++
                return v / 100.0
            }
            return v
        }

        fun power(): Double {
            val base = factor()
            if (peek() == '^') {
                i++
                return base.pow(power())
            }
            return base
        }

        fun product(): Double {
            var v = power()
            while (peek() == '*' || peek() == '/') {
                val op = src[i++]
                val rhs = power()
                v = if (op == '*') v * rhs else v / rhs
            }
            return v
        }

        parseSum = {
            var v = product()
            while (peek() == '+' || peek() == '-') {
                val op = src[i++]
                val rhs = product()
                v = if (op == '+') v + rhs else v - rhs
            }
            v
        }

        val result = parseSum()
        if (i != src.length) throw IllegalArgumentException("Unexpected '${src[i]}' at position ${i + 1}.")
        if (!result.isFinite()) throw IllegalArgumentException("Result is not a finite number.")
        return result
    }
}
