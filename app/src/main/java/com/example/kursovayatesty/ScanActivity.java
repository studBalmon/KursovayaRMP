package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Запускаем сканер сразу
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
        // Проверяем, что текст содержит хотя бы один %% — иначе это не тест
        if (!content.contains("%%")) {
            Toast.makeText(this, "Ошибка: это не тест (отсутствует %% в тексте)", Toast.LENGTH_LONG).show();
            return;
        }

        File testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();

        // Название теста — всё до первого %%
        String fileName;
        int separatorIndex = content.indexOf("%%");
        if (separatorIndex != -1) {
            fileName = content.substring(0, separatorIndex).trim();
        } else {
            fileName = "test_" + System.currentTimeMillis();
        }

        // Заменим запрещённые символы в имени файла
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

        File testFile = new File(testsFolder, fileName + ".txt");

        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(content.getBytes());
            Toast.makeText(this, "Файл сохранён: " + fileName + ".txt", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        finish(); // Закрыть ScanActivity
    }


}
