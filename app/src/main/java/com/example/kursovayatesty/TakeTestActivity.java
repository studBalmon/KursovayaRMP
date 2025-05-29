package com.example.kursovayatesty;

import android.content.Intent;
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

public class TakeTestActivity extends AppCompatActivity {

    private LinearLayout questionsLayout;
    private Button submitButton;
    private List<Question> questions = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_test);

        questionsLayout = findViewById(R.id.questionsLayout);
        submitButton = findViewById(R.id.submitButton);

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

        submitButton.setOnClickListener(v -> checkAnswers());
        setupBottomNav();
    }

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

    private void loadTestFromJson(String json) {
        try {
            Gson gson = new Gson();
            Test test = gson.fromJson(json, Test.class);
            this.questions = test.getQuestions();
            displayQuestions();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка разбора теста", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void displayQuestions() {
        LayoutInflater inflater = LayoutInflater.from(this);
        questionsLayout.removeAllViews();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            View view = inflater.inflate(R.layout.item_question_take, null);

            TextView questionText = view.findViewById(R.id.questionText);
            RadioGroup radioGroup = view.findViewById(R.id.answersGroup);

            questionText.setText((i + 1) + ". " + q.getText());

            for (int j = 0; j < q.getOptions().size(); j++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(q.getOptions().get(j));
                rb.setId(j);
                radioGroup.addView(rb);
            }

            int finalI = i;
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                questions.get(finalI).setSelectedAnswerIndex(checkedId);
            });

            questionsLayout.addView(view);
        }
    }

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

        Toast.makeText(this, "Правильных: " + correct + " из " + questions.size(), Toast.LENGTH_LONG).show();
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
}


