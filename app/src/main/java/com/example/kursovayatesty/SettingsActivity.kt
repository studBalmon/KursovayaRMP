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
    private var languageRadioGroup: RadioGroup? = null
    private var themeRadioGroup: RadioGroup? = null
    private var saveButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        saveButton = findViewById(R.id.saveSettingsButton)

        setupBottomNav()
        loadSettings()

        saveButton?.setOnClickListener {
            saveSettings()
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, "English")!!
        val theme = prefs.getString(KEY_THEME, "Light")!!

        if (language == "English") {
            languageRadioGroup!!.check(R.id.rbEnglish)
        } else if (language == "Русский") {
            languageRadioGroup!!.check(R.id.rbRussian)
        }

        when (theme) {
            "Light" -> themeRadioGroup!!.check(R.id.rbLight)
            "Dark" -> themeRadioGroup!!.check(R.id.rbDark)
            "Special" -> themeRadioGroup!!.check(R.id.rbSpecial)
        }
    }

    private fun saveSettings() {
        var language = "English"
        var theme = "Light"

        val selectedLangId = languageRadioGroup!!.checkedRadioButtonId
        if (selectedLangId == R.id.rbEnglish) {
            language = "English"
        } else if (selectedLangId == R.id.rbRussian) {
            language = "Русский"
        }

        val selectedThemeId = themeRadioGroup!!.checkedRadioButtonId
        if (selectedThemeId == R.id.rbLight) {
            theme = "Light"
        } else if (selectedThemeId == R.id.rbDark) {
            theme = "Dark"
        } else if (selectedThemeId == R.id.rbSpecial) {
            theme = "Special"
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_LANGUAGE, language)
        editor.putString(KEY_THEME, theme)
        editor.apply()

        val intent = Intent(this@SettingsActivity, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
        startActivity(intent)
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_settings

        nav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_test -> {
                    startActivity(Intent(this, TestListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_create -> {
                    startActivity(Intent(this, CreateTestActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun applySelectedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val theme = prefs.getString(KEY_THEME, "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

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
        private const val PREFS_NAME = "app_settings"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
    }
}
