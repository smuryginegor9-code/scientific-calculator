package com.example.engineeringcalculator.domain

class CalculationException(val error: CalculatorError) : IllegalArgumentException()
