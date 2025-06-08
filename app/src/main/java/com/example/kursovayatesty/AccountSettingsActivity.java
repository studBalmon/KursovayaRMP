package com.example.kursovayatesty;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class AccountSettingsActivity extends AppCompatActivity {

    private EditText newEmailEditText, newPasswordEditText;
    private Button updateEmailButton, updatePasswordButton, logoutButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView avatarImageView;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();         // Применяет язык, выбранный в настройках
        applySelectedTheme();    // Применяет тему оформления
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Если пользователь не авторизован — вернуться на LoginActivity
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }




        // Инициализация элементов UI
        newEmailEditText = findViewById(R.id.newEmailEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        updateEmailButton = findViewById(R.id.updateEmailButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
        avatarImageView = findViewById(R.id.avatarImageView);


        setupBottomNav();

        // Смена email
        updateEmailButton.setOnClickListener(v -> {
            String newEmail = newEmailEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newEmail)) {
                Toast.makeText(this, "Введите новый email", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Email обновлён", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Смена пароля
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Выход из аккаунта
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
    private void applySelectedTheme() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Light");

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
     * Применяет язык интерфейса, выбранный пользователем.
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
}
