package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.viewmodel.LoginViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)

        if (loginViewModel.checkUserLoggedIn()) {
            navigateToAccountSettings()
            return
        }

        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            loginViewModel.login(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        registerButton.setOnClickListener {
            loginViewModel.register(emailEditText.text.toString(), passwordEditText.text.toString())
        }

        setupObservers()
        setupBottomNav()
    }

    private fun setupObservers() {
        loginViewModel.loginResult.observe(this, Observer { result ->
            if (result.success) {
                navigateToAccountSettings()
            } else {
                Toast.makeText(this, "Ошибка входа: ${result.errorMessage ?: "Неизвестная ошибка"}", Toast.LENGTH_SHORT).show()
            }
        })

        loginViewModel.registerResult.observe(this, Observer { result ->
            if (result.success) {
                Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка регистрации: ${result.errorMessage ?: "Неизвестная ошибка"}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToAccountSettings() {
        startActivity(Intent(this, AccountSettingsActivity::class.java))
        finish()
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_menu

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

                else -> {
                    Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
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
