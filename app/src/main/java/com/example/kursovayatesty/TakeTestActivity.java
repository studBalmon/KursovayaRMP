package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;

public class TakeTestActivity extends AppCompatActivity {

    private LinearLayout questionsContainer;
    private Button checkAnswersButton;
    private List<Question> questions = new ArrayList<>();

    private class Question {
        String questionText;
        String[] options = new String[4];
        int correctAnswerIndex;
        int selectedAnswerIndex = -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_test);

        questionsContainer = findViewById(R.id.questionsContainer);
        checkAnswersButton = findViewById(R.id.checkAnswersButton);

        String fileName = getIntent().getStringExtra("test_file_name");
        if (fileName == null) {
            Toast.makeText(this, "Ошибка: не указан файл теста", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTestFromFile(fileName);
        displayQuestions();

        checkAnswersButton.setOnClickListener(v -> checkAnswers());

        setupBottomNav();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadTestFromFile(String fileName) {
        File testsFolder = new File(getFilesDir(), "Tests");
        File testFile = new File(testsFolder, fileName);

        try {
            String content = new String(Files.readAllBytes(testFile.toPath()));
            // Формат: title%%question%%option1%%option2%%option3%%option4%%correctIndex%%question%%...
            String[] parts = content.split("%%");
            // Первый элемент - название теста, пропустим
            int pos = 1;

            while (pos + 6 < parts.length) {
                Question q = new Question();
                q.questionText = parts[pos++];
                for (int i = 0; i < 4; i++) {
                    q.options[i] = parts[pos++];
                }
                q.correctAnswerIndex = Integer.parseInt(parts[pos++]);
                questions.add(q);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка чтения теста", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayQuestions() {
        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            View questionView = inflater.inflate(R.layout.question_item_take_test, null);

            TextView questionTextView = questionView.findViewById(R.id.questionTextView);
            RadioGroup radioGroup = questionView.findViewById(R.id.radioGroupAnswers);

            questionTextView.setText((i + 1) + ". " + q.questionText);

            for (int j = 0; j < 4; j++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(q.options[j]);
                rb.setId(j);
                radioGroup.addView(rb);
            }

            int finalI = i;
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                questions.get(finalI).selectedAnswerIndex = checkedId;
            });

            questionsContainer.addView(questionView);
        }

        // Показать контейнер и кнопку после добавления вопросов
        questionsContainer.setVisibility(View.VISIBLE);
        checkAnswersButton.setVisibility(View.VISIBLE);
    }


    private void checkAnswers() {
        int correctCount = 0;
        int unanswered = 0;

        for (Question q : questions) {
            if (q.selectedAnswerIndex == -1) {
                unanswered++;
            } else if (q.selectedAnswerIndex == q.correctAnswerIndex) {
                correctCount++;
            }
        }

        if (unanswered > 0) {
            Toast.makeText(this, "Ответьте на все вопросы", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                "Результат: " + correctCount + " из " + questions.size(),
                Toast.LENGTH_LONG).show();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_test);
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
            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

}