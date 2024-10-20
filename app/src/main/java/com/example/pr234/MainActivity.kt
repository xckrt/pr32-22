package com.example.pr234

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var cityEditText: EditText
    private lateinit var weatherInfoTextView: TextView
    private lateinit var getWeatherButton: Button
    private val apiKey = "fca737f3e02f11d8a26fe1e1be828ecd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityEditText = findViewById(R.id.cityEditText)
        weatherInfoTextView = findViewById(R.id.weatherInfoTextView)
        getWeatherButton = findViewById(R.id.getWeatherButton)

        getWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString()
            getWeatherData(city)
        }
    }

    private fun getWeatherData(city: String) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric&lang=ru"

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            parseWeatherData(response)
        }, { error ->
            Log.e("WeatherApp", "Error fetching weather data: $error")
        })

        queue.add(stringRequest)
    }

    private fun parseWeatherData(response: String) {
        val jsonObject = JSONObject(response)
        val main = jsonObject.getJSONObject("main")
        val temp = main.getDouble("temp")
        val pressure = main.getInt("pressure")
        val wind = jsonObject.getJSONObject("wind").getDouble("speed")
        val weatherDescription = jsonObject.getJSONArray("weather")
            .getJSONObject(0).getString("description")

        val weatherInfo = """
            Температура: $temp°C
            Давление: $pressure гПа
            Скорость ветра: $wind м/с
            Описание: $weatherDescription
        """.trimIndent()

        weatherInfoTextView.text = weatherInfo
    }
}