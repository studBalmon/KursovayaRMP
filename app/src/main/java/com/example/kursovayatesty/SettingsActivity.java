package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";

    private RadioGroup languageRadioGroup, themeRadioGroup;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        saveButton = findViewById(R.id.saveSettingsButton);
        setupBottomNav();

        loadSettings();

        saveButton.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(SettingsActivity.this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
            finish(); // Можно закрыть после сохранения
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "English");
        String theme = prefs.getString(KEY_THEME, "Light");

        // Язык
        if (language.equals("English")) {
            languageRadioGroup.check(R.id.rbEnglish);
        } else if (language.equals("Русский")) {
            languageRadioGroup.check(R.id.rbRussian);
        }

        // Тема
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

    private void saveSettings() {
        String language = "English";
        String theme = "Light";

        int selectedLangId = languageRadioGroup.getCheckedRadioButtonId();
        if (selectedLangId == R.id.rbEnglish) {
            language = "English";
        } else if (selectedLangId == R.id.rbRussian) {
            language = "Русский";
        }

        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        if (selectedThemeId == R.id.rbLight) {
            theme = "Light";
        } else if (selectedThemeId == R.id.rbDark) {
            theme = "Dark";
        } else if (selectedThemeId == R.id.rbSpecial) {
            theme = "Special";
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_settings);
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
}
