package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CreateTestActivity extends AppCompatActivity {

    private EditText testTitleEditText;
    private LinearLayout questionsContainer;
    private List<View> questionViews = new ArrayList<>();
    private File testsFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_test);

        testTitleEditText = findViewById(R.id.testTitleEditText);
        questionsContainer = findViewById(R.id.questionsContainer);
        Button addQuestionButton = findViewById(R.id.addQuestionButton);
        Button saveTestButton = findViewById(R.id.saveTestButton);

        testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();

        addQuestionButton.setOnClickListener(v -> addQuestionView());
        saveTestButton.setOnClickListener(v -> saveTestToFile());

        setupBottomNav();
        addQuestionView(); // добавить первый вопрос
    }

    private void addQuestionView() {
        View questionView = getLayoutInflater().inflate(R.layout.question_item, null);
        questionsContainer.addView(questionView);
        questionViews.add(questionView);

        RadioGroup radioGroup = questionView.findViewById(R.id.radioGroup);
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            RadioButton radio = (RadioButton) radioGroup.getChildAt(i);
            radio.setOnClickListener(v -> radioGroup.check(radio.getId()));
        }
    }

    private void saveTestToFile() {
        String title = testTitleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название теста", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append(title).append("%%");

        for (View qView : questionViews) {
            EditText questionEdit = qView.findViewById(R.id.questionEditText);
            EditText[] options = {
                    qView.findViewById(R.id.answer1EditText),
                    qView.findViewById(R.id.answer2EditText),
                    qView.findViewById(R.id.answer3EditText),
                    qView.findViewById(R.id.answer4EditText),
            };
            RadioGroup radioGroup = qView.findViewById(R.id.radioGroup);
            int checkedId = radioGroup.getCheckedRadioButtonId();

            if (questionEdit.getText().toString().trim().isEmpty() || checkedId == -1) {
                Toast.makeText(this, "Заполните все вопросы и выберите правильные ответы", Toast.LENGTH_SHORT).show();
                return;
            }

            content.append(questionEdit.getText().toString().trim()).append("%%");

            for (int i = 0; i < 4; i++) {
                content.append(options[i].getText().toString().trim()).append("%%");
            }

            for (int i = 0; i < 4; i++) {
                if (radioGroup.getChildAt(i).getId() == checkedId) {
                    content.append(i).append("%%");
                    break;
                }
            }
        }

        File testFile = new File(testsFolder, title + ".txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(content.toString().getBytes());
            Toast.makeText(this, "Тест сохранён", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
        }
    }

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
}
