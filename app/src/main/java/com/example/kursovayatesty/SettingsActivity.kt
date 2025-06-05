package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    // Элементы интерфейса
    private var languageRadioGroup: RadioGroup? = null
    private var themeRadioGroup: RadioGroup? = null
    private var saveButton: Button? = null

    /**
     * Метод жизненного цикла, вызывается при создании активности.
     * Применяет язык и тему, загружает интерфейс, устанавливает обработчики.
     *
     * @param savedInstanceState Состояние активности (не используется)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage() // Установка языка интерфейса
        applySelectedTheme() // Установка темы оформления
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // Установка макета

        // Поиск элементов интерфейса
        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        saveButton = findViewById(R.id.saveSettingsButton)

        setupBottomNav() // Настройка нижнего навигационного меню
        loadSettings() // Загрузка сохранённых настроек в интерфейс

        // Обработчик кнопки сохранения
        saveButton?.setOnClickListener {
            saveSettings()
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    /**
     * Загружает сохранённые настройки из SharedPreferences
     * и отображает их в RadioGroup'ах.
     */
    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, "English")!!
        val theme = prefs.getString(KEY_THEME, "Light")!!

        // Отметить текущий язык
        if (language == "English") {
            languageRadioGroup!!.check(R.id.rbEnglish)
        } else if (language == "Русский") {
            languageRadioGroup!!.check(R.id.rbRussian)
        }

        // Отметить текущую тему
        when (theme) {
            "Light" -> themeRadioGroup!!.check(R.id.rbLight)
            "Dark" -> themeRadioGroup!!.check(R.id.rbDark)
            "Special" -> themeRadioGroup!!.check(R.id.rbSpecial)
        }
    }

    /**
     * Сохраняет выбранные настройки языка и темы в SharedPreferences.
     * Затем перезапускает активность для применения новых настроек.
     */
    private fun saveSettings() {
        var language = "English"
        var theme = "Light"

        // Получить выбранный язык
        val selectedLangId = languageRadioGroup!!.checkedRadioButtonId
        if (selectedLangId == R.id.rbEnglish) {
            language = "English"
        } else if (selectedLangId == R.id.rbRussian) {
            language = "Русский"
        }

        // Получить выбранную тему
        val selectedThemeId = themeRadioGroup!!.checkedRadioButtonId
        if (selectedThemeId == R.id.rbLight) {
            theme = "Light"
        } else if (selectedThemeId == R.id.rbDark) {
            theme = "Dark"
        } else if (selectedThemeId == R.id.rbSpecial) {
            theme = "Special"
        }

        // Сохраняем в SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_LANGUAGE, language)
        editor.putString(KEY_THEME, theme)
        editor.apply()

        // Перезапуск активности для применения новых настроек
        val intent = Intent(
            this@SettingsActivity,
            SettingsActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        finish() // Завершаем текущую
        startActivity(intent) // Запускаем заново
    }

    /**
     * Настраивает нижнее меню навигации и обработчики нажатий.
     */
    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_settings // Устанавливаем текущий пункт

        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            if (id == R.id.nav_test) {
                startActivity(
                    Intent(
                        this,
                        TestListActivity::class.java
                    )
                )
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_create) {
                startActivity(
                    Intent(
                        this,
                        CreateTestActivity::class.java
                    )
                )
                finish()
            }
            if (id == R.id.nav_menu) {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_scan) {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_settings) {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
                finish()
                return@setOnItemSelectedListener true
            }
            true
        }
    }

    /**
     * Применяет выбранную тему оформления до вызова setContentView.
     * Получает тему из SharedPreferences и применяет соответствующий стиль.
     */
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val theme = prefs.getString(KEY_THEME, "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

    /**
     * Применяет язык интерфейса до загрузки макета.
     * Получает язык из SharedPreferences и обновляет конфигурацию.
     */
    private fun applyLanguage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, "English")!!

        val localeCode = if (language == "Русский") "ru" else "en"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    companion object {
        // Константы для ключей настроек
        private const val PREFS_NAME = "app_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }
}

