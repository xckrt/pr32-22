package com.example.pr234

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        loginButton = findViewById(R.id.loginButton)
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Проверяем, сохранены ли данные пользователя
        val savedUsername = sharedPreferences.getString("username", null)

        if (savedUsername != null) {
            // Если данные уже сохранены, предлагаем пользователю перейти на главный экран
            showWelcomeMessageAndProceed(savedUsername)
        } else {
            // Если данных нет, предоставляем возможность ввести их
            loginButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                if (username.isNotEmpty()) {
                    saveUsername(username)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    usernameEditText.error = "Введите имя пользователя"
                }
            }
        }
    }

    // Функция для отображения приветственного сообщения и перехода на главный экран
    private fun showWelcomeMessageAndProceed(username: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username", username)  // Передаем имя пользователя на главный экран
        startActivity(intent)
        finish()
    }

    // Функция для сохранения имени пользователя в SharedPreferences
    private fun saveUsername(username: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.apply()
    }
}
