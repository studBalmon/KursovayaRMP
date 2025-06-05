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
    // Поля для ввода email и пароля, а также экземпляр FirebaseAuth для аутентификации
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage() // Применяет язык, выбранный в настройках
        applySelectedTheme() // Применяет тему оформления
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Привязка UI-элементов
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        // Обработка нажатия кнопок входа и регистрации
        loginButton.setOnClickListener { v: View? -> login() }
        registerButton.setOnClickListener { v: View? -> register() }

        // Настройка нижнего навигационного меню
        setupBottomNav()
    }

    /**
     * Авторизация пользователя с помощью email и пароля.
     * Получает данные из полей ввода и вызывает FirebaseAuth для входа.
     */
    private fun login() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val pass = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // При успешном входе — переход в главное меню
                    startActivity(Intent(this, MenuActivity::class.java))
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

    /**
     * Регистрация нового пользователя.
     * Получает email и пароль из полей и регистрирует пользователя через Firebase.
     */
    private fun register() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val pass = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Ошибка регистрации: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Настройка нижнего меню навигации.
     * В зависимости от выбранного пункта запускает соответствующую активность.
     */
    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_menu // Устанавливаем текущую вкладку

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
                return@setOnItemSelectedListener true
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

            // Шутливое сообщение по умолчанию
            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show()
            true
        }
    }

    /**
     * Применяет выбранную пользователем тему оформления.
     * Получает данные из SharedPreferences.
     */
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light) // Светлая тема
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark) // Тёмная тема
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special) // Специальная тема
        }
    }

    /**
     * Применяет выбранный язык интерфейса.
     * Получает значение из SharedPreferences и устанавливает локаль.
     */
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

