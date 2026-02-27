package com.example.engineeringcalculator.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.engineeringcalculator.data.CalculationRepository
import com.example.engineeringcalculator.domain.CalculatorEngine

@Suppress("DEPRECATION")
class CalculatorViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: CalculationRepository,
    private val engine: CalculatorEngine = CalculatorEngine(),
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository, engine, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
