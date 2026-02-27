package com.example.engineeringcalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engineeringcalculator.data.CalculationRepository
import com.example.engineeringcalculator.domain.AngleUnit
import com.example.engineeringcalculator.domain.CalculationException
import com.example.engineeringcalculator.domain.CalculatorEngine
import com.example.engineeringcalculator.domain.CalculatorError
import com.example.engineeringcalculator.util.NumberFormatter
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val repository: CalculationRepository,
    private val engine: CalculatorEngine,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _expression = savedStateHandle.getLiveData(KEY_EXPRESSION, "")
    val expression: LiveData<String> = _expression

    private val _result = savedStateHandle.getLiveData(KEY_RESULT, "")
    val result: LiveData<String> = _result

    private val _error = savedStateHandle.getLiveData<CalculatorError?>(KEY_ERROR, null)
    val error: LiveData<CalculatorError?> = _error

    private val _angleUnit = savedStateHandle.getLiveData(KEY_ANGLE_UNIT, AngleUnit.DEG)
    val angleUnit: LiveData<AngleUnit> = _angleUnit

    fun appendToken(rawToken: String) {
        val token = normalizeToken(rawToken)
        val current = _expression.value.orEmpty()
        val merged = StringBuilder(current)

        if (shouldInsertImplicitMultiplication(current, token)) {
            merged.append('*')
        }

        merged.append(token)
        _expression.value = merged.toString()
        _error.value = null
    }

    fun deleteLast() {
        val current = _expression.value.orEmpty()
        if (current.isEmpty()) return

        _expression.value = current.dropLast(1)
        _error.value = null
    }

    fun clearAll() {
        _expression.value = ""
        _result.value = ""
        _error.value = null
    }

    fun evaluate() {
        val expressionValue = _expression.value.orEmpty()
        if (expressionValue.isBlank()) {
            _error.value = CalculatorError.INVALID_EXPRESSION
            return
        }

        try {
            val value = engine.evaluate(expressionValue, _angleUnit.value ?: AngleUnit.DEG)
            val formatted = NumberFormatter.format(value)
            _result.value = formatted
            _error.value = null

            viewModelScope.launch {
                repository.saveSuccessfulCalculation(expressionValue, formatted)
            }
        } catch (exception: CalculationException) {
            _error.value = exception.error
        } catch (_: Exception) {
            _error.value = CalculatorError.INVALID_EXPRESSION
        }
    }

    fun setAngleUnit(unit: AngleUnit) {
        if (_angleUnit.value == unit) return
        _angleUnit.value = unit
    }

    private fun normalizeToken(token: String): String {
        return when (token) {
            "," -> "."
            else -> token
        }
    }

    private fun shouldInsertImplicitMultiplication(current: String, nextToken: String): Boolean {
        if (current.isEmpty()) return false
        if (!startsImplicitMultiplicationToken(nextToken)) return false

        val last = current.last()
        return last.isDigit() || last == ')' || last == '!' || last == '²' || last == '.'
    }

    private fun startsImplicitMultiplicationToken(token: String): Boolean {
        if (token.isEmpty()) return false
        if (token == CalculatorEngine.PI_LITERAL || token == CalculatorEngine.E_LITERAL) return true

        val first = token.first()
        return first == '(' || first == 'π' || first.isLetter()
    }

    companion object {
        private const val KEY_EXPRESSION = "expression"
        private const val KEY_RESULT = "result"
        private const val KEY_ERROR = "error"
        private const val KEY_ANGLE_UNIT = "angle_unit"
    }
}
