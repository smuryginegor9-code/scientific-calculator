package com.example.engineeringcalculator

import android.app.Application
import androidx.room.Room
import com.example.engineeringcalculator.data.AppDatabase
import com.example.engineeringcalculator.data.CalculationRepository
import com.google.android.material.color.DynamicColors

class EngineeringCalculatorApp : Application() {

    lateinit var repository: CalculationRepository
        private set

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()

        repository = CalculationRepository(database.calculationDao())
    }

    companion object {
        private const val DATABASE_NAME = "calculator-db"
    }
}
