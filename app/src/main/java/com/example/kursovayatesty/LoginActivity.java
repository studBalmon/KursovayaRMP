package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.*;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    // Поля для ввода email и пароля, а также экземпляр FirebaseAuth для аутентификации
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();         // Применяет язык, выбранный в настройках
        applySelectedTheme();    // Применяет тему оформления

        super.onCreate(savedInstanceState);

        // Инициализация Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Проверка: если пользователь уже вошёл, переходим в настройки аккаунта
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, AccountSettingsActivity.class); // замените на вашу активность
            startActivity(intent);
            finish(); // закрываем LoginActivity
            return;
        }

        //Если пользователь не вошёл, показываем экран авторизации
        setContentView(R.layout.activity_login);

        // Привязка UI-элементов
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Обработка нажатия кнопок входа и регистрации
        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> register());

        // Настройка нижнего навигационного меню
        setupBottomNav();
    }


    /**
     * Авторизация пользователя с помощью email и пароля.
     * Получает данные из полей ввода и вызывает FirebaseAuth для входа.
     */
    private void login() {
        String email = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // При успешном входе — переход в главное меню
                        startActivity(new Intent(this, AccountSettingsActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Регистрация нового пользователя.
     * Получает email и пароль из полей и регистрирует пользователя через Firebase.
     */
    private void register() {
        String email = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Настройка нижнего меню навигации.
     * В зависимости от выбранного пункта запускает соответствующую активность.
     */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_menu); // Устанавливаем текущую вкладку

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_test) {
                startActivity(new Intent(this, TestListActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateTestActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_scan) {
                startActivity(new Intent(this, ScanActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }

            // Шутливое сообщение по умолчанию
            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * Применяет выбранную пользователем тему оформления.
     * Получает данные из SharedPreferences.
     */
    private void applySelectedTheme() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Light");

        switch (theme) {
            case "Light":
                setTheme(R.style.Theme_KursovayaTesty_Light); // Светлая тема
                break;
            case "Dark":
                setTheme(R.style.Theme_KursovayaTesty_Dark); // Тёмная тема
                break;
            case "Special":
                setTheme(R.style.Theme_KursovayaTesty_Special); // Специальная тема
                break;
        }
    }

    /**
     * Применяет выбранный язык интерфейса.
     * Получает значение из SharedPreferences и устанавливает локаль.
     */
    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String language = prefs.getString("language", "English");

        String localeCode = language.equals("Русский") ? "ru" : "en";
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}

