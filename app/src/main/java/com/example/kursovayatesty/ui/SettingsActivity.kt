package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.ui.viewmodel.SettingsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var saveButton: Button

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        saveButton = findViewById(R.id.saveSettingsButton)

        setupBottomNav()

        viewModel.language.observe(this, Observer { lang ->
            when (lang) {
                "English" -> languageRadioGroup.check(R.id.rbEnglish)
                "Русский" -> languageRadioGroup.check(R.id.rbRussian)
            }
        })

        viewModel.theme.observe(this, Observer { theme ->
            when (theme) {
                "Light" -> themeRadioGroup.check(R.id.rbLight)
                "Dark" -> themeRadioGroup.check(R.id.rbDark)
                "Special" -> themeRadioGroup.check(R.id.rbSpecial)
            }
        })

        viewModel.message.observe(this, Observer { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (msg == "Настройки сохранены") {
                val intent = Intent(this@SettingsActivity, SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
                startActivity(intent)
            }
        })

        viewModel.loadSettings()

        saveButton.setOnClickListener {
            val language = when (languageRadioGroup.checkedRadioButtonId) {
                R.id.rbEnglish -> "English"
                R.id.rbRussian -> "Русский"
                else -> "English"
            }
            val theme = when (themeRadioGroup.checkedRadioButtonId) {
                R.id.rbLight -> "Light"
                R.id.rbDark -> "Dark"
                R.id.rbSpecial -> "Special"
                else -> "Light"
            }
            viewModel.saveSettings(language, theme)
        }
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
        const val PREFS_NAME = "app_settings"
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME = "theme"
    }
}
