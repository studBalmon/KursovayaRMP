package com.example.kursovayatesty;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        findViewById(R.id.saveToCloudButton).setOnClickListener(v -> {
            String title = getTestTitle();
            List<Question> questions = collectQuestions();
            saveTestToCloud(title, questions);
        });


        setupBottomNav();
        addQuestionView();
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

        Log.d("CreateTestActivity", "Сериализованный JSON теста:\n" + json);  // вывод в лог

        File testFile = new File(testsFolder, title + ".json");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Тест сохранён (JSON)", Toast.LENGTH_SHORT).show();
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

    private void saveTestToCloud(String title, List<Question> questionList) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Test test = new Test(title, questionList); // ✅ создаём полноценный объект
        Gson gson = new Gson();
        String json = gson.toJson(test); // ✅ сериализуем

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("title", title); // сохраняем отдельно — удобно для списка
        testMap.put("content", json); // ✅ вся структура теста — в строку

        db.collection("users")
                .document(userId)
                .collection("cloud_tests")
                .add(testMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Тест сохранён в облаке", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при сохранении: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private List<Question> collectQuestions() {
        List<Question> questionList = new ArrayList<>();
        LinearLayout questionsContainer = findViewById(R.id.questionsContainer);

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


    private String getTestTitle() {
        return testTitleEditText.getText().toString().trim();
    }



}
