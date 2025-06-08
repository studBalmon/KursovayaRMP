package com.example.kursovayatesty;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    // Константы для ключей настроек
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";

    // Элементы интерфейса
    private RadioGroup languageRadioGroup, themeRadioGroup;
    private Button saveButton;

    /**
     * Метод жизненного цикла, вызывается при создании активности.
     * Применяет язык и тему, загружает интерфейс, устанавливает обработчики.
     *
     * @param savedInstanceState Состояние активности (не используется)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();          // Установка языка интерфейса
        applySelectedTheme();     // Установка темы оформления
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Установка макета

        // Поиск элементов интерфейса
        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        saveButton = findViewById(R.id.saveSettingsButton);

        setupBottomNav();
        loadSettings();

        // Обработчик кнопки сохранения
        saveButton.setOnClickListener(v -> {
            saveSettings(); // Сохраняет настройки
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
            finish(); // Закрываем активность
        });
    }

    /**
     * Загружает сохранённые настройки из SharedPreferences
     * и отображает их в RadioGroup'ах.
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "English");
        String theme = prefs.getString(KEY_THEME, "Light");

        // Отметить текущий язык
        if (language.equals("English")) {
            languageRadioGroup.check(R.id.rbEnglish);
        } else if (language.equals("Русский")) {
            languageRadioGroup.check(R.id.rbRussian);
        }

        // Отметить текущую тему
        switch (theme) {
            case "Light":
                themeRadioGroup.check(R.id.rbLight);
                break;
            case "Dark":
                themeRadioGroup.check(R.id.rbDark);
                break;
            case "Special":
                themeRadioGroup.check(R.id.rbSpecial);
                break;
        }
    }

    /**
     * Сохраняет выбранные настройки языка и темы в SharedPreferences.
     * Затем перезапускает активность для применения новых настроек.
     */
    private void saveSettings() {
        String language = "English";
        String theme = "Light";

        // Получить выбранный язык
        int selectedLangId = languageRadioGroup.getCheckedRadioButtonId();
        if (selectedLangId == R.id.rbEnglish) {
            language = "English";
        } else if (selectedLangId == R.id.rbRussian) {
            language = "Русский";
        }

        // Получить выбранную тему
        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        if (selectedThemeId == R.id.rbLight) {
            theme = "Light";
        } else if (selectedThemeId == R.id.rbDark) {
            theme = "Dark";
        } else if (selectedThemeId == R.id.rbSpecial) {
            theme = "Special";
        }

        // Сохраняем в SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.putString(KEY_THEME, theme);
        editor.apply();

        // Перезапуск активности для применения новых настроек
        Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish(); // Завершаем текущую
        startActivity(intent); // Запускаем заново
    }

    /**
     * Настраивает нижнее меню навигации и обработчики нажатий.
     */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_settings); // Устанавливаем текущий пункт

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
            return true;
        });
    }

    /**
     * Применяет выбранную тему оформления до вызова setContentView.
     * Получает тему из SharedPreferences и применяет соответствующий стиль.
     */
    private void applySelectedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString(KEY_THEME, "Light");

        switch (theme) {
            case "Light":
                setTheme(R.style.Theme_KursovayaTesty_Light);
                break;
            case "Dark":
                setTheme(R.style.Theme_KursovayaTesty_Dark);
                break;
            case "Special":
                setTheme(R.style.Theme_KursovayaTesty_Special);
                break;
        }
    }

    /**
     * Применяет язык интерфейса до загрузки макета.
     * Получает язык из SharedPreferences и обновляет конфигурацию.
     */
    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "English");

        String localeCode = language.equals("Русский") ? "ru" : "en";
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}

