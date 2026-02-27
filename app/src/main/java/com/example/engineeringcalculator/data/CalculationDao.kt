package com.example.engineeringcalculator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Insert
    suspend fun insert(entity: CalculationEntity)

    @Query("SELECT * FROM calculations ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<CalculationEntity>>

    @Query("DELETE FROM calculations")
    suspend fun clearAll()
}
