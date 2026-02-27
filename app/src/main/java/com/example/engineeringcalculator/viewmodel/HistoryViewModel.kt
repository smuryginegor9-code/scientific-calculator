package com.example.engineeringcalculator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.engineeringcalculator.data.CalculationEntity
import com.example.engineeringcalculator.data.CalculationRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: CalculationRepository
) : ViewModel() {

    val history: LiveData<List<CalculationEntity>> = repository
        .observeHistory(HISTORY_LIMIT)
        .asLiveData()

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    companion object {
        private const val HISTORY_LIMIT = 100
    }
}
