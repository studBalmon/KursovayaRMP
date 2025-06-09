package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale


class AccountSettingsActivity : AppCompatActivity() {
    private var newEmailEditText: EditText? = null
    private var newPasswordEditText: EditText? = null
    private var updateEmailButton: Button? = null
    private var updatePasswordButton: Button? = null
    private var logoutButton: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }


        newEmailEditText = findViewById(R.id.newEmailEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        updateEmailButton = findViewById(R.id.updateEmailButton)
        updatePasswordButton = findViewById(R.id.updatePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)


        setupBottomNav()

        updateEmailButton?.setOnClickListener {
            val newEmail = newEmailEditText?.text.toString().trim()
            if (TextUtils.isEmpty(newEmail)) {
                Toast.makeText(this, "Введите новый email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentUser!!.updateEmail(newEmail)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email обновлён", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        updatePasswordButton?.setOnClickListener {
            val newPassword = newPasswordEditText?.text.toString().trim()
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentUser!!.updatePassword(newPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        logoutButton?.setOnClickListener(View.OnClickListener { v: View? ->
            mAuth!!.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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

            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
