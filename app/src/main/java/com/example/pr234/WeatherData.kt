package com.example.pr234

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val temperature: Double,
    val pressure: Int,
    val windSpeed: Double,
    val description: String
)
