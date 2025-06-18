package com.example.kursovayatesty.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.ui.viewmodel.MenuViewModel
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    private val viewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySelectedTheme()
        applyLanguage()
        viewModel.loadSettings()

        viewModel.theme.observe(this) { theme ->
            when (theme) {
                "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
                "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
                "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
            }
        }

        viewModel.language.observe(this) { language ->
            applyLanguage(language)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<View>(R.id.buttonCreate).setOnClickListener {
            startActivity(Intent(this, CreateTestActivity::class.java))
        }

        findViewById<View>(R.id.buttonTests).setOnClickListener {
            startActivity(Intent(this, TestListActivity::class.java))
        }

        findViewById<View>(R.id.authMenu).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<View>(R.id.buttonScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        findViewById<View>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun applyLanguage(language: String) {
        val localeCode = if (language == "Русский") "ru" else "en"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!
        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = prefs.getString("language", "English")!!
        val localeCode = if (language == "Русский") "ru" else "en"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
