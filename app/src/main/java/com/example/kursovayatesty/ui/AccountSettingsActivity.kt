package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.viewmodel.AccountSettingsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class AccountSettingsActivity : AppCompatActivity() {

    private val viewModel: AccountSettingsViewModel by viewModels()

    private lateinit var newEmailEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var updateEmailButton: Button
    private lateinit var updatePasswordButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        newEmailEditText = findViewById(R.id.newEmailEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        updateEmailButton = findViewById(R.id.updateEmailButton)
        updatePasswordButton = findViewById(R.id.updatePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)

        if (viewModel.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        updateEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()
            if (TextUtils.isEmpty(newEmail)) {
                Toast.makeText(this, "Введите новый email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updateEmail(newEmail)
        }

        updatePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updatePassword(newPassword)
        }

        logoutButton.setOnClickListener {
            viewModel.logout()
        }

        observeViewModel()
        setupBottomNav()
    }

    private fun observeViewModel() {
        viewModel.updateEmailResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Email обновлён", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.updatePasswordResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.logoutEvent.observe(this, Observer { loggedOut ->
            if (loggedOut) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })
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
}
