package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TakeTestActivity extends AppCompatActivity {

    private LinearLayout questionsLayout; // Контейнер для вопросов (вёрстка)
    private Button submitButton;          // Кнопка отправки/проверки ответов
    private List<Question> questions = new ArrayList<>(); // Список вопросов теста
    private String testTitle;

    /**
     * Метод жизненного цикла активности.
     * Загружает настройки языка и темы,
     * затем инициализирует интерфейс,
     * загружает тест из JSON (переданный через Intent) или из файла,
     * устанавливает обработчик кнопки отправки и навигации.
     *
     * @param savedInstanceState сохранённое состояние (не используется)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        applySelectedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_test);

        questionsLayout = findViewById(R.id.questionsLayout);
        submitButton = findViewById(R.id.submitButton);

        // Проверяем источник теста: JSON из облака или имя файла
        if (getIntent().hasExtra("test_content")) {
            String json = getIntent().getStringExtra("test_content");
            Log.d("TakeTestActivity", "Loaded test from cloud/json: " + json);
            loadTestFromJson(json);
        } else if (getIntent().hasExtra("test_file_name")) {
            String fileName = getIntent().getStringExtra("test_file_name");
            loadTestFromFile(fileName);
        } else {
            Toast.makeText(this, "Источник теста не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Обработка нажатия кнопки "Отправить"
        submitButton.setOnClickListener(v -> checkAnswers());

        setupBottomNav();
    }

    /**
     * Загружает тест из файла по имени.
     * Читает весь файл как строку, затем парсит JSON.
     *
     * @param fileName имя файла с тестом в папке "Tests"
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadTestFromFile(String fileName) {
        try {
            File file = new File(getFilesDir(), "Tests/" + fileName);
            String content = new String(Files.readAllBytes(file.toPath()));
            loadTestFromJson(content);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка загрузки файла", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Загружает тест из JSON-строки.
     * Парсит JSON с помощью Gson в объект Test,
     * сохраняет список вопросов и отображает их.
     *
     * @param json строка с JSON-тестом
     */
    private void loadTestFromJson(String json) {
        try {
            Gson gson = new Gson();
            Test test = gson.fromJson(json, Test.class);
            this.testTitle = test.getTitle();
            this.questions = test.getQuestions();
            displayQuestions();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка разбора теста", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Отображает вопросы на экране.
     * Для каждого вопроса создаёт отдельный View с текстом вопроса и вариантами ответов (RadioButton).
     * Добавляет слушатель выбора варианта, чтобы записать ответ пользователя в модель вопроса.
     */
    private void displayQuestions() {
        LayoutInflater inflater = LayoutInflater.from(this);
        questionsLayout.removeAllViews();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            View view = inflater.inflate(R.layout.item_question_take, null);

            TextView questionText = view.findViewById(R.id.questionText);
            RadioGroup radioGroup = view.findViewById(R.id.answersGroup);

            questionText.setText((i + 1) + ". " + q.getText());

            // Добавляем варианты ответов в RadioGroup
            for (int j = 0; j < q.getOptions().size(); j++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(q.getOptions().get(j));
                rb.setId(j);
                radioGroup.addView(rb);
            }

            int finalI = i;
            // При выборе варианта ответа обновляем поле selectedAnswerIndex в вопросе
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                questions.get(finalI).setSelectedAnswerIndex(checkedId);
            });

            questionsLayout.addView(view);
        }
    }

    /**
     * Проверяет ответы пользователя.
     * Подсчитывает количество правильных ответов и отсутствующих (не выбранных).
     * Если есть вопросы без ответов — выводит предупреждение.
     * Иначе показывает количество правильных ответов.
     */
    private void checkAnswers() {
        int correct = 0;
        int unanswered = 0;

        for (Question q : questions) {
            if (q.getSelectedAnswerIndex() == -1) {
                unanswered++;
            } else if (q.getSelectedAnswerIndex() == q.getCorrectIndex()) {
                correct++;
            }
        }

        if (unanswered > 0) {
            Toast.makeText(this, "Ответьте на все вопросы", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("correct", correct);
        intent.putExtra("testName", testTitle);
        intent.putExtra("total", questions.size());
        startActivity(intent);
        finish();
    }


    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_test);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_test) {
                startActivity(new Intent(this, TestListActivity.class));
            } else if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateTestActivity.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
            } else if (id == R.id.nav_scan) {
                startActivity(new Intent(this, ScanActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            finish();
            return true;
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



