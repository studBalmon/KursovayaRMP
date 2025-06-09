package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        applySelectedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, TestListActivity.class);
            startActivity(intent);
            finish();
        });


        PieChart pieChart = findViewById(R.id.pieChart);
        TextView resultText = findViewById(R.id.resultText);

        String testName = getIntent().getStringExtra("testName");
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 1); // Не делим на 0

        float percentage = ((float) correct / total) * 100;
        // Локальное сохранение
        ScoreManager.saveBestScore(this, testName, percentage);

        // Firebase сохранение
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference scoreRef = db.collection("users").document(user.getUid())
                    .collection("test_scores").document(testName);

            scoreRef.get().addOnSuccessListener(doc -> {
                float cloudBest = doc.contains("best_percent") ? doc.getDouble("best_percent").floatValue() : 0f;
                if (percentage > cloudBest) {
                    scoreRef.set(Collections.singletonMap("best_percent", percentage));
                }
            });
        }
        resultText.setText("Правильных: " + correct + " из " + total + " (" + (int) percentage + "%)");

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(correct, "Верные"));
        entries.add(new PieEntry(total - correct, "Ошибки"));

        PieDataSet dataSet = new PieDataSet(entries, "Результаты");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(16f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Результат");
        pieChart.setCenterTextSize(20f);
        pieChart.setHoleRadius(40f);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(14f);

        pieChart.invalidate(); // Обновить диаграмму
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
