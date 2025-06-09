package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth!!.currentUser != null) {
            val intent = Intent(this, AccountSettingsActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener { login() }
        registerButton.setOnClickListener { register() }

        setupBottomNav()
    }

    private fun login() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val pass = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, AccountSettingsActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Ошибка входа: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun register() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val pass = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Ошибка регистрации: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
