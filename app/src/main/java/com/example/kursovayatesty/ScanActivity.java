package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new IntentIntegrator(this)
                .setPrompt("Отсканируйте QR-код теста")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                saveTestToFile(result.getContents());
            } else {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void saveTestToFile(String content) {
        try {
            // Проверим, что content — это JSON и содержит поле "title"
            JSONObject jsonObject = new JSONObject(content);

            String fileName = jsonObject.optString("title", "test_" + System.currentTimeMillis());
            fileName = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

            File testsFolder = new File(getFilesDir(), "Tests");
            if (!testsFolder.exists()) testsFolder.mkdirs();

            File testFile = new File(testsFolder, fileName + ".json");

            try (FileOutputStream fos = new FileOutputStream(testFile)) {
                fos.write(content.getBytes());
                Toast.makeText(this, "Файл сохранён: " + fileName + ".json", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Ошибка: это невалидный JSON-тест", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        finish(); // Закрыть ScanActivity
    }



}
