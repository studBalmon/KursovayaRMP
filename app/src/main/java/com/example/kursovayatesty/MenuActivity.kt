package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage() // Устанавливает язык интерфейса из настроек
        applySelectedTheme() // Применяет выбранную пользователем тему

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu) // Загружает разметку главного меню

        // Кнопка "Создать тест" — переходит на экран создания теста
        findViewById<View>(R.id.buttonCreate).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    CreateTestActivity::class.java
                )
            )
        }

        // Кнопка "Список тестов" — открывает список доступных тестов
        findViewById<View>(R.id.buttonTests).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    TestListActivity::class.java
                )
            )
        }

        // Кнопка "Авторизация" — переход к экрану входа и регистрации
        findViewById<View>(R.id.authMenu).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
        }

        // Кнопка "Сканировать" — переход к экрану сканирования QR-кода
        findViewById<View>(R.id.buttonScan).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    ScanActivity::class.java
                )
            )
        }

        // Кнопка "Настройки" — переход к экрану настроек
        findViewById<View>(R.id.buttonSettings).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
    }

    /**
     * Применяет тему оформления, выбранную пользователем в настройках.
     * Темы: Light, Dark, Special.
     */
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light) // Светлая тема
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark) // Тёмная тема
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special) // Особая тема
        }
    }

    /**
     * Применяет язык интерфейса, выбранный пользователем в настройках.
     * Доступные языки: English и Русский.
     */
    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = prefs.getString("language", "English")!!

        // Определение кода локали в зависимости от языка
        val localeCode = if (language == "Русский") "ru" else "en"

        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        // Применение новой локали
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

