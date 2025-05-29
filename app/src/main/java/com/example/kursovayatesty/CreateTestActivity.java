package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Активность для создания теста: добавление вопросов, сохранение на устройство и в облако.
 */
public class CreateTestActivity extends AppCompatActivity {

    // Поля интерфейса
    private EditText testTitleEditText; // поле для ввода названия теста
    private LinearLayout questionsContainer; // контейнер для всех вопросов
    private List<View> questionViews = new ArrayList<>(); // список всех view вопросов
    private File testsFolder; // папка, где будут храниться локальные JSON-файлы тестов

    /**
     * Метод жизненного цикла: вызывается при создании активности.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage(); // применить язык из настроек
        applySelectedTheme(); // применить тему из настроек
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        // Инициализация UI-элементов
        testTitleEditText = findViewById(R.id.testTitleEditText);
        questionsContainer = findViewById(R.id.questionsContainer);
        Button addQuestionButton = findViewById(R.id.addQuestionButton);
        Button saveTestButton = findViewById(R.id.saveTestButton);

        // Создаём папку для хранения тестов, если её нет
        testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();

        // Кнопка: добавить вопрос
        addQuestionButton.setOnClickListener(v -> addQuestionView());

        // Кнопка: сохранить тест в файл
        saveTestButton.setOnClickListener(v -> saveTestToFile());

        // Кнопка: сохранить тест в облако (Firebase)
        findViewById(R.id.saveToCloudButton).setOnClickListener(v -> {
            String title = getTestTitle();
            List<Question> questions = collectQuestions();
            saveTestToCloud(title, questions);
        });

        setupBottomNav(); // навигационное меню
        addQuestionView(); // сразу добавляем 1 вопрос при создании
    }

    /**
     * Добавляет один блок вопроса в интерфейс.
     * Инициализирует переключатели ответов.
     */
    private void addQuestionView() {
        View questionView = getLayoutInflater().inflate(R.layout.question_item, null);
        questionsContainer.addView(questionView);
        questionViews.add(questionView);

        RadioGroup radioGroup = questionView.findViewById(R.id.radioGroup);

        // Установка логики: при нажатии на радиокнопку она становится выбранной
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            RadioButton radio = (RadioButton) radioGroup.getChildAt(i);
            radio.setOnClickListener(v -> radioGroup.check(radio.getId()));
        }
    }

    /**
     * Сохраняет тест в файл в формате JSON.
     */
    private void saveTestToFile() {
        String title = getTestTitle();
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название теста", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Question> questions = collectQuestions();
        if (questions.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы один вопрос", Toast.LENGTH_SHORT).show();
            return;
        }

        Test test = new Test(title, questions);
        Gson gson = new Gson();
        String json = gson.toJson(test);

        Log.d("CreateTestActivity", "Сериализованный JSON теста:\n" + json);

        File testFile = new File(testsFolder, title + ".json");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Тест сохранён (JSON)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Обработка нажатий в нижней навигации.
     */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_create);
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
     * Сохраняет тест в Firebase Firestore в коллекцию cloud_tests пользователя.
     *
     * @param title        Название теста.
     * @param questionList Список вопросов.
     */
    private void saveTestToCloud(String title, List<Question> questionList) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Test test = new Test(title, questionList);
        Gson gson = new Gson();
        String json = gson.toJson(test);

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("title", title);
        testMap.put("content", json);

        db.collection("users")
                .document(userId)
                .collection("cloud_tests")
                .add(testMap)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Тест сохранён в облаке", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Собирает все введённые вопросы и их ответы с экрана.
     *
     * @return Список объектов Question.
     */
    private List<Question> collectQuestions() {
        List<Question> questionList = new ArrayList<>();

        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View questionView = questionsContainer.getChildAt(i);

            EditText questionEditText = questionView.findViewById(R.id.questionEditText);
            EditText[] optionEdits = {
                    questionView.findViewById(R.id.answer1EditText),
                    questionView.findViewById(R.id.answer2EditText),
                    questionView.findViewById(R.id.answer3EditText),
                    questionView.findViewById(R.id.answer4EditText),
            };
            RadioGroup radioGroup = questionView.findViewById(R.id.radioGroup);
            int checkedId = radioGroup.getCheckedRadioButtonId();

            List<String> options = new ArrayList<>();
            int correctIndex = -1;

            for (int j = 0; j < 4; j++) {
                options.add(optionEdits[j].getText().toString());

                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(j);
                if (radioButton.getId() == checkedId) {
                    correctIndex = j;
                }
            }

            String questionText = questionEditText.getText().toString().trim();

            if (!questionText.isEmpty() && options.size() == 4 && correctIndex != -1) {
                questionList.add(new Question(questionText, options, correctIndex));
            }
        }

        return questionList;
    }

    /**
     * Получает название теста из поля ввода.
     *
     * @return Строка — заголовок теста.
     */
    private String getTestTitle() {
        return testTitleEditText.getText().toString().trim();
    }

    /**
     * Применяет тему, выбранную пользователем в настройках.
     */
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
}
