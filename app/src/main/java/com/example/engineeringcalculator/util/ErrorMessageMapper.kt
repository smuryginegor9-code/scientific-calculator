package com.example.engineeringcalculator.util

import androidx.annotation.StringRes
import com.example.engineeringcalculator.R
import com.example.engineeringcalculator.domain.CalculatorError

@StringRes
fun CalculatorError.toMessageRes(): Int {
    return when (this) {
        CalculatorError.INVALID_EXPRESSION -> R.string.error_invalid_expression
        CalculatorError.UNKNOWN_TOKEN -> R.string.error_unknown_token
        CalculatorError.MISMATCHED_PARENTHESES -> R.string.error_parentheses
        CalculatorError.DIVISION_BY_ZERO -> R.string.error_division_by_zero
        CalculatorError.NEGATIVE_SQRT -> R.string.error_negative_sqrt
        CalculatorError.NON_POSITIVE_LOG -> R.string.error_non_positive_log
        CalculatorError.TAN_UNDEFINED -> R.string.error_tan_undefined
        CalculatorError.FACTORIAL_NON_INTEGER -> R.string.error_factorial_non_integer
        CalculatorError.FACTORIAL_NEGATIVE -> R.string.error_factorial_negative
        CalculatorError.FACTORIAL_LIMIT -> R.string.error_factorial_limit
        CalculatorError.RESULT_NOT_REAL -> R.string.error_result_not_real
    }
}
