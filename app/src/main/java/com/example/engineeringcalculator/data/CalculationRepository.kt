package com.example.engineeringcalculator.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(
    private val dao: CalculationDao
) {

    fun observeHistory(limit: Int = 50): Flow<List<CalculationEntity>> {
        return dao.observeRecent(limit)
    }

    suspend fun saveSuccessfulCalculation(expression: String, result: String) {
        dao.insert(
            CalculationEntity(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
