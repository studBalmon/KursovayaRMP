package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        applySelectedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // Загружает разметку главного меню

        // Кнопка "Создать тест" — переходит на экран создания теста
        findViewById(R.id.buttonCreate).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateTestActivity.class));
        });

        // Кнопка "Список тестов" — открывает список доступных тестов
        findViewById(R.id.buttonTests).setOnClickListener(v -> {
            startActivity(new Intent(this, TestListActivity.class));
        });

        // Кнопка "Авторизация" — переход к экрану входа и регистрации
        findViewById(R.id.authMenu).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Кнопка "Сканировать" — переход к экрану сканирования QR-кода
        findViewById(R.id.buttonScan).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
        });

        // Кнопка "Настройки" — переход к экрану настроек
        findViewById(R.id.buttonSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    /**
     * Применяет тему оформления, выбранную пользователем в настройках.
     * Темы: Light, Dark, Special.
     */
    private void applySelectedTheme() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Light");

        switch (theme) {
            case "Light":
                setTheme(R.style.Theme_KursovayaTesty_Light); // Светлая тема
                break;
            case "Dark":
                setTheme(R.style.Theme_KursovayaTesty_Dark);  // Тёмная тема
                break;
            case "Special":
                setTheme(R.style.Theme_KursovayaTesty_Special); // Особая тема
                break;
        }
    }

    /**
     * Применяет язык интерфейса, выбранный пользователем в настройках.
     * Доступные языки: English и Русский.
     */
    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String language = prefs.getString("language", "English");

        // Определение кода локали в зависимости от языка
        String localeCode = language.equals("Русский") ? "ru" : "en";

        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);

        // Применение новой локали
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}

