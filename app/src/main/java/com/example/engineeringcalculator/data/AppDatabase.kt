package com.example.engineeringcalculator.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CalculationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
}
