package com.example.pr234
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface WeatherDao {
    @Insert
    fun insert(weatherData: WeatherData)

    @Update
    fun update(weatherData: WeatherData)

    @Delete
    fun delete(weatherData: WeatherData)

    @Query("SELECT * FROM weather_data WHERE city = :city LIMIT 1")
    fun getWeatherByCity(city: String): WeatherData?

    @Query("SELECT * FROM weather_data")
    fun getAllWeatherData(): List<WeatherData>

    @Query("DELETE FROM weather_data")
    fun deleteAll()
}
