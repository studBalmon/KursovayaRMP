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

/**
 * Активность для сканирования QR-кода с тестом.
 * После успешного сканирования JSON-тест сохраняется в файл.
 */
public class ScanActivity extends AppCompatActivity {

    /**
     * Метод жизненного цикла, вызывается при создании активности.
     * Запускает сканер QR-кодов с помощью ZXing IntentIntegrator.
     *
     * @param savedInstanceState Сохранённое состояние активности (не используется здесь)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализация и запуск сканера QR-кода
        new IntentIntegrator(this)
                .setPrompt("Отсканируйте QR-код теста") // Сообщение на экране сканирования
                .setBeepEnabled(true) // Включить звуковой сигнал при сканировании
                .setOrientationLocked(true) // Заблокировать ориентацию экрана
                .initiateScan(); // Запуск сканирования
    }

    /**
     * Обработка результата сканирования QR-кода.
     *
     * @param requestCode Код запроса
     * @param resultCode  Код результата (например, RESULT_OK)
     * @param data        Интент с результатами сканирования
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Парсинг результата сканирования
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Если есть содержимое — сохранить его как тест
                saveTestToFile(result.getContents());
            } else {
                // Если пользователь отменил сканирование
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Если результат не связан со сканированием — передаём дальше
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Сохраняет отсканированный JSON-тест в файл во внутренней директории приложения.
     *
     * @param content Строка с JSON-тестом, полученная из QR-кода
     */
    private void saveTestToFile(String content) {
        try {
            // Преобразуем строку в JSON-объект
            JSONObject jsonObject = new JSONObject(content);

            // Получаем название файла из поля "title", если оно есть, иначе — генерируем
            String fileName = jsonObject.optString("title", "test_" + System.currentTimeMillis());

            // Удаляем недопустимые символы в имени файла
            fileName = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

            // Создаём папку "Tests" во внутреннем хранилище, если она ещё не существует
            File testsFolder = new File(getFilesDir(), "Tests");
            if (!testsFolder.exists()) testsFolder.mkdirs();

            // Создаём файл JSON с именем testName.json
            File testFile = new File(testsFolder, fileName + ".json");

            // Записываем содержимое в файл
            try (FileOutputStream fos = new FileOutputStream(testFile)) {
                fos.write(content.getBytes());
                Toast.makeText(this, "Файл сохранён: " + fileName + ".json", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } catch (JSONException e) {
            // Если строка не является валидным JSON
            Toast.makeText(this, "Ошибка: это невалидный JSON-тест", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Завершаем активность после сохранения (или ошибки)
        finish();
    }
}
