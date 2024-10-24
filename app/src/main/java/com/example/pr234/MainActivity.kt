package com.example.pr234
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
class MainActivity : AppCompatActivity() {
    private lateinit var deleteDataButton: Button
    private lateinit var checkDataButton: Button
    private lateinit var cityEditText: EditText
    private lateinit var getWeatherButton: Button
    private lateinit var temperatureEditText: EditText
    private lateinit var pressureEditText: EditText
    private lateinit var windSpeedEditText: EditText
    private lateinit var updateWeatherButton: Button
    private lateinit var descriptionEditText: EditText
    val apiKey = "475642488e68f8d03911fb348cc9f536"
    private lateinit var weatherDao: WeatherDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = WeatherDatabase.getDatabase(this)
        weatherDao = db.weatherDao()
        cityEditText = findViewById(R.id.cityEditText)
        getWeatherButton = findViewById(R.id.getWeatherButton)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        temperatureEditText = findViewById(R.id.temperatureEditText)
        pressureEditText = findViewById(R.id.pressureEditText)
        windSpeedEditText = findViewById(R.id.windSpeedEditText)
        updateWeatherButton = findViewById(R.id.updateWeatherButton)
        checkDataButton = findViewById(R.id.checkDataButton)
        deleteDataButton = findViewById(R.id.DeleteDataButton)
        deleteDataButton.setOnClickListener {
            deleteAllWeatherData()
        }
        checkDataButton.setOnClickListener {
            checkDatabaseContents()
        }
        getWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString()
            getWeatherData(city)
        }
        updateWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString()
            val temperature = temperatureEditText.text.toString().toDoubleOrNull()
            val pressure = pressureEditText.text.toString().toIntOrNull()
            val windSpeed = windSpeedEditText.text.toString().toDoubleOrNull()
            val description = descriptionEditText.text.toString() // Получение описания

            if (temperature != null && pressure != null && windSpeed != null) {
                val updatedWeatherData = WeatherData(
                    city = city,
                    temperature = temperature,
                    pressure = pressure,
                    windSpeed = windSpeed,
                    description = description // Добавление описания
                )

                lifecycleScope.launch {
                    updateWeatherData(updatedWeatherData)
                    updateEditTextFields(updatedWeatherData)
                }
            }
        }
    }
    private fun checkDatabaseContents() {
        lifecycleScope.launch {
            val allWeatherData = withContext(Dispatchers.IO) {
                weatherDao.getAllWeatherData()
            }
            Log.d("WeatherApp", "All weather data: $allWeatherData")
            // Обрабатывайте данные здесь, если нужно
        }
    }
    private fun getWeatherData(city: String) {
        lifecycleScope.launch {
            val existingWeatherData = withContext(Dispatchers.IO) {
                weatherDao.getWeatherByCity(city)
            }

            if (existingWeatherData != null) {
                // Если данные найдены в базе данных, отображаем их в EditText
                updateEditTextFields(existingWeatherData)
            } else {
                // Если данных нет в базе, запрашиваем их из API
                fetchWeatherFromApi(city)
            }
        }
    }

    private fun deleteAllWeatherData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                weatherDao.deleteAll()
            }
            Log.d("WeatherApp", "All weather data deleted")
        }
    }
    private fun fetchWeatherFromApi(city: String) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric&lang=ru"

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            lifecycleScope.launch {
                // Парсим данные и обновляем поля EditText
                parseWeatherData(response)
            }
        }, { error ->
            Log.e("WeatherApp", "Error fetching weather data: $error")
            // Если ошибка, то можно очистить поля EditText
            clearEditTextFields()
        })

        queue.add(stringRequest)
    }
    private suspend fun parseWeatherData(response: String) {
        val jsonObject = JSONObject(response)
        val main = jsonObject.getJSONObject("main")
        val temp = main.getDouble("temp")
        val pressure = main.getInt("pressure")
        val wind = jsonObject.getJSONObject("wind").getDouble("speed")
        val weatherDescription = jsonObject.getJSONArray("weather")
            .getJSONObject(0).getString("description")

        val cityName = jsonObject.getString("name")

        // Проверяем, есть ли уже данные для этого города
        val existingWeatherData = withContext(Dispatchers.IO) {
            weatherDao.getWeatherByCity(cityName)
        }

        val weatherData = if (existingWeatherData != null) {
            // Используем ID существующей записи для обновления
            existingWeatherData.copy(
                temperature = temp,
                pressure = pressure,
                windSpeed = wind,
                description = weatherDescription
            )
        } else {
            // Создаем новый объект WeatherData (ID будет автоматически сгенерирован при вставке)
            WeatherData(
                city = cityName,
                temperature = temp,
                pressure = pressure,
                windSpeed = wind,
                description = weatherDescription
            )
        }



        // Вставляем или обновляем данные
        if (existingWeatherData == null) {
            insertWeatherData(weatherData)
        } else {
            updateWeatherData(weatherData)
        }

        updateEditTextFields(weatherData)
    }
    private suspend fun insertWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            val existingWeatherData = weatherDao.getWeatherByCity(weatherData.city)
            if (existingWeatherData != null) {
                // Если данные о городе уже существуют, обновляем их
                weatherDao.update(weatherData.copy(id = existingWeatherData.id))
                Log.d("WeatherApp", "Updated weather data: $weatherData") // Логируем обновление
            } else {
                // Если данных о городе нет, добавляем новые
                weatherDao.insert(weatherData)
                Log.d("WeatherApp", "Inserted new weather data: $weatherData") // Логируем вставку
            }
        }
    }
    private suspend fun updateWeatherData(weatherData: WeatherData) {
        withContext(Dispatchers.IO) {
            // Проверяем наличие данных в базе перед обновлением
            val existingWeather = weatherDao.getWeatherByCity(weatherData.city)
            if (existingWeather != null) {
                // Обновляем данные, используя существующий ID
                weatherDao.update(weatherData.copy(id = existingWeather.id))
                Log.d("WeatherApp", "Updated weather data: $weatherData")
            } else {
                Log.d("WeatherApp", "No existing data to update for city: ${weatherData.city}")
            }
        }
    }
    private fun updateEditTextFields(weatherData: WeatherData) {
        temperatureEditText.setText(weatherData.temperature.toString())
        pressureEditText.setText(weatherData.pressure.toString())
        windSpeedEditText.setText(weatherData.windSpeed.toString())
        descriptionEditText.setText(weatherData.description)
    }
    private fun clearEditTextFields() {
        temperatureEditText.text.clear()
        pressureEditText.text.clear()
        windSpeedEditText.text.clear()
        descriptionEditText.text.clear()
    }
}

