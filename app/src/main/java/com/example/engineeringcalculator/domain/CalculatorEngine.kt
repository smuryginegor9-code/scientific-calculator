package com.example.engineeringcalculator.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculatorEngine {

    fun evaluate(rawExpression: String, angleUnit: AngleUnit): Double {
        val expression = normalize(rawExpression)
        if (expression.isBlank()) throw CalculationException(CalculatorError.INVALID_EXPRESSION)

        val tokens = tokenize(expression)
        val rpn = toRpn(tokens)
        return evaluateRpn(rpn, angleUnit)
    }

    private fun normalize(value: String): String {
        return value
            .replace("\u2212", "-")
            .replace("×", "*")
            .replace("÷", "/")
            .replace(',', '.')
            .trim()
    }

    private fun tokenize(expression: String): List<Token> {
        val result = mutableListOf<Token>()
        var index = 0

        while (index < expression.length) {
            val ch = expression[index]

            when {
                ch.isWhitespace() -> {
                    index++
                }

                ch.isDigit() || ch == '.' -> {
                    var cursor = index
                    var dotCount = 0
                    while (cursor < expression.length &&
                        (expression[cursor].isDigit() || expression[cursor] == '.')
                    ) {
                        if (expression[cursor] == '.') dotCount++
                        cursor++
                    }

                    if (dotCount > 1) throw CalculationException(CalculatorError.INVALID_EXPRESSION)
                    val number = expression.substring(index, cursor).toDoubleOrNull()
                        ?: throw CalculationException(CalculatorError.INVALID_EXPRESSION)
                    result.add(Token.Number(number))
                    index = cursor
                }

                ch.isLetter() -> {
                    var cursor = index
                    while (cursor < expression.length &&
                        (expression[cursor].isLetterOrDigit())
                    ) {
                        cursor++
                    }

                    val identifier = expression.substring(index, cursor).lowercase()
                    when (identifier) {
                        "pi" -> result.add(Token.Number(PI_VALUE))
                        "e" -> result.add(Token.Number(E_VALUE))
                        "sin", "cos", "tan", "tg", "ln", "log", "log10", "sqrt" -> {
                            val normalizedName = when (identifier) {
                                "tg" -> "tan"
                                "log" -> "log10"
                                else -> identifier
                            }
                            result.add(Token.Function(normalizedName))
                        }

                        else -> throw CalculationException(CalculatorError.UNKNOWN_TOKEN)
                    }
                    index = cursor
                }

                ch == 'π' -> {
                    result.add(Token.Number(PI_VALUE))
                    index++
                }

                ch == '°' -> {
                    // Degree symbol is optional in the expression and can be ignored.
                    index++
                }

                ch in SIMPLE_TOKENS -> {
                    val token = when (ch) {
                        '(' -> Token.LeftParen
                        ')' -> Token.RightParen
                        else -> Token.Operator(ch.toString())
                    }
                    result.add(token)
                    index++
                }

                ch == '√' -> {
                    result.add(Token.Function("sqrt"))
                    index++
                }

                else -> throw CalculationException(CalculatorError.UNKNOWN_TOKEN)
            }
        }

        return result
    }

    // Converts infix notation into Reverse Polish Notation via shunting-yard.
    private fun toRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val operators = ArrayDeque<Token>()
        var previous: Token? = null

        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    output.add(token)
                    previous = token
                }

                is Token.Function -> {
                    operators.addLast(token)
                    previous = token
                }

                is Token.LeftParen -> {
                    operators.addLast(token)
                    previous = token
                }

                is Token.RightParen -> {
                    var foundLeft = false
                    while (operators.isNotEmpty()) {
                        val top = operators.removeLast()
                        if (top is Token.LeftParen) {
                            foundLeft = true
                            break
                        }
                        output.add(top)
                    }

                    if (!foundLeft) throw CalculationException(CalculatorError.MISMATCHED_PARENTHESES)

                    if (operators.lastOrNull() is Token.Function) {
                        output.add(operators.removeLast())
                    }

                    previous = token
                }

                is Token.Operator -> {
                    var symbol = token.symbol
                    if ((symbol == "+" || symbol == "-") && shouldTreatAsUnary(previous)) {
                        if (symbol == "+") {
                            // Unary plus is a no-op and can be ignored.
                            continue
                        }
                        symbol = UNARY_MINUS
                    }

                    val currentInfo = OPERATOR_INFO[symbol]
                        ?: throw CalculationException(CalculatorError.INVALID_EXPRESSION)

                    while (operators.isNotEmpty()) {
                        val top = operators.last()
                        if (top !is Token.Operator) break

                        val topInfo = OPERATOR_INFO[top.symbol]
                            ?: throw CalculationException(CalculatorError.INVALID_EXPRESSION)

                        val shouldPop =
                            (currentInfo.associativity == Associativity.LEFT &&
                                currentInfo.precedence <= topInfo.precedence) ||
                                (currentInfo.associativity == Associativity.RIGHT &&
                                    currentInfo.precedence < topInfo.precedence)

                        if (!shouldPop) break
                        output.add(operators.removeLast())
                    }

                    operators.addLast(Token.Operator(symbol))
                    previous = Token.Operator(symbol)
                }
            }
        }

        while (operators.isNotEmpty()) {
            val top = operators.removeLast()
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw CalculationException(CalculatorError.MISMATCHED_PARENTHESES)
            }
            output.add(top)
        }

        return output
    }

    private fun shouldTreatAsUnary(previous: Token?): Boolean {
        return when (previous) {
            null -> true
            is Token.Number -> false
            is Token.RightParen -> false
            is Token.Operator -> !isPostfix(previous.symbol)
            is Token.Function, is Token.LeftParen -> true
        }
    }

    private fun isPostfix(symbol: String): Boolean {
        return symbol == FACTORIAL || symbol == SQUARE
    }

    // Evaluates RPN stack and checks domain errors for real-number calculator mode.
    private fun evaluateRpn(rpn: List<Token>, angleUnit: AngleUnit): Double {
        val values = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> values.addLast(token.value)

                is Token.Operator -> {
                    when (token.symbol) {
                        PLUS -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            values.addLast(checkFinite(left + right))
                        }

                        MINUS -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            values.addLast(checkFinite(left - right))
                        }

                        MULTIPLY -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            values.addLast(checkFinite(left * right))
                        }

                        DIVIDE -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            if (abs(right) < EPS) throw CalculationException(CalculatorError.DIVISION_BY_ZERO)
                            values.addLast(checkFinite(left / right))
                        }

                        POWER -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            values.addLast(checkFinite(left.pow(right)))
                        }

                        PERCENT -> {
                            val right = popValue(values)
                            val left = popValue(values)
                            values.addLast(checkFinite(right * left / 100.0))
                        }

                        UNARY_MINUS -> {
                            val value = popValue(values)
                            values.addLast(checkFinite(-value))
                        }

                        FACTORIAL -> {
                            val value = popValue(values)
                            values.addLast(checkFinite(factorial(value).toDouble()))
                        }

                        SQUARE -> {
                            val value = popValue(values)
                            values.addLast(checkFinite(value * value))
                        }

                        else -> throw CalculationException(CalculatorError.INVALID_EXPRESSION)
                    }
                }

                is Token.Function -> {
                    val value = popValue(values)
                    val evaluated = when (token.name) {
                        "sin" -> {
                            val radians = toRadiansIfNeeded(value, angleUnit)
                            sin(radians)
                        }

                        "cos" -> {
                            val radians = toRadiansIfNeeded(value, angleUnit)
                            cos(radians)
                        }

                        "tan" -> {
                            val radians = toRadiansIfNeeded(value, angleUnit)
                            if (abs(cos(radians)) < TAN_UNDEFINED_EPS) {
                                throw CalculationException(CalculatorError.TAN_UNDEFINED)
                            }
                            tan(radians)
                        }

                        "sqrt" -> {
                            if (value < 0.0) throw CalculationException(CalculatorError.NEGATIVE_SQRT)
                            sqrt(value)
                        }

                        "ln" -> {
                            if (value <= 0.0) throw CalculationException(CalculatorError.NON_POSITIVE_LOG)
                            ln(value)
                        }

                        "log10" -> {
                            if (value <= 0.0) throw CalculationException(CalculatorError.NON_POSITIVE_LOG)
                            log10(value)
                        }

                        else -> throw CalculationException(CalculatorError.UNKNOWN_TOKEN)
                    }
                    values.addLast(checkFinite(evaluated))
                }

                is Token.LeftParen, is Token.RightParen -> {
                    throw CalculationException(CalculatorError.INVALID_EXPRESSION)
                }
            }
        }

        if (values.size != 1) throw CalculationException(CalculatorError.INVALID_EXPRESSION)
        return checkFinite(values.last())
    }

    private fun toRadiansIfNeeded(value: Double, angleUnit: AngleUnit): Double {
        return if (angleUnit == AngleUnit.DEG) Math.toRadians(value) else value
    }

    private fun factorial(value: Double): Long {
        val rounded = value.roundToLong()
        if (abs(value - rounded.toDouble()) > INTEGER_EPS) {
            throw CalculationException(CalculatorError.FACTORIAL_NON_INTEGER)
        }
        if (rounded < 0) {
            throw CalculationException(CalculatorError.FACTORIAL_NEGATIVE)
        }
        if (rounded > FACTORIAL_LIMIT) {
            throw CalculationException(CalculatorError.FACTORIAL_LIMIT)
        }

        var result = 1L
        for (i in 2L..rounded) {
            result *= i
        }
        return result
    }

    private fun popValue(stack: ArrayDeque<Double>): Double {
        if (stack.isEmpty()) throw CalculationException(CalculatorError.INVALID_EXPRESSION)
        return stack.removeLast()
    }

    private fun checkFinite(value: Double): Double {
        if (!value.isFinite()) throw CalculationException(CalculatorError.RESULT_NOT_REAL)
        if (value.isNaN()) throw CalculationException(CalculatorError.RESULT_NOT_REAL)
        return value
    }

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data class Function(val name: String) : Token
        data class Operator(val symbol: String) : Token
        data object LeftParen : Token
        data object RightParen : Token
    }

    private enum class Associativity {
        LEFT,
        RIGHT
    }

    private data class OperatorInfo(
        val precedence: Int,
        val associativity: Associativity
    )

    companion object {
        const val PI_LITERAL = "3.1415926536"
        const val E_LITERAL = "2.7182818285"

        private const val PLUS = "+"
        private const val MINUS = "-"
        private const val MULTIPLY = "*"
        private const val DIVIDE = "/"
        private const val POWER = "^"
        private const val PERCENT = "%"
        private const val FACTORIAL = "!"
        private const val SQUARE = "²"
        private const val UNARY_MINUS = "u-"

        private const val EPS = 1e-12
        private const val INTEGER_EPS = 1e-10
        private const val TAN_UNDEFINED_EPS = 1e-10
        private const val FACTORIAL_LIMIT = 20L

        private val PI_VALUE = PI_LITERAL.toDouble()
        private val E_VALUE = E_LITERAL.toDouble()

        private val SIMPLE_TOKENS = setOf('(', ')', '+', '-', '*', '/', '^', '%', '!', '²')

        private val OPERATOR_INFO = mapOf(
            PLUS to OperatorInfo(precedence = 1, associativity = Associativity.LEFT),
            MINUS to OperatorInfo(precedence = 1, associativity = Associativity.LEFT),
            MULTIPLY to OperatorInfo(precedence = 2, associativity = Associativity.LEFT),
            DIVIDE to OperatorInfo(precedence = 2, associativity = Associativity.LEFT),
            PERCENT to OperatorInfo(precedence = 2, associativity = Associativity.LEFT),
            POWER to OperatorInfo(precedence = 4, associativity = Associativity.RIGHT),
            UNARY_MINUS to OperatorInfo(precedence = 3, associativity = Associativity.RIGHT),
            FACTORIAL to OperatorInfo(precedence = 5, associativity = Associativity.LEFT),
            SQUARE to OperatorInfo(precedence = 5, associativity = Associativity.LEFT)
        )
    }
}
