package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Активность для сканирования QR-кода с тестом.
 * После успешного сканирования JSON-тест сохраняется в файл.
 */
class ScanActivity : AppCompatActivity() {
    /**
     * Метод жизненного цикла, вызывается при создании активности.
     * Запускает сканер QR-кодов с помощью ZXing IntentIntegrator.
     *
     * @param savedInstanceState Сохранённое состояние активности (не используется здесь)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация и запуск сканера QR-кода
        IntentIntegrator(this)
            .setPrompt("Отсканируйте QR-код теста") // Сообщение на экране сканирования
            .setBeepEnabled(true) // Включить звуковой сигнал при сканировании
            .setOrientationLocked(true) // Заблокировать ориентацию экрана
            .initiateScan() // Запуск сканирования
    }

    /**
     * Обработка результата сканирования QR-кода.
     *
     * @param requestCode Код запроса
     * @param resultCode  Код результата (например, RESULT_OK)
     * @param data        Интент с результатами сканирования
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Парсинг результата сканирования
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Если есть содержимое — сохранить его как тест
                saveTestToFile(result.contents)
            } else {
                // Если пользователь отменил сканирование
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            // Если результат не связан со сканированием — передаём дальше
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Сохраняет отсканированный JSON-тест в файл во внутренней директории приложения.
     *
     * @param content Строка с JSON-тестом, полученная из QR-кода
     */
    private fun saveTestToFile(content: String) {
        try {
            // Преобразуем строку в JSON-объект
            val jsonObject = JSONObject(content)

            // Получаем название файла из поля "title", если оно есть, иначе — генерируем
            var fileName = jsonObject.optString("title", "test_" + System.currentTimeMillis())

            // Удаляем недопустимые символы в имени файла
            fileName = fileName.trim { it <= ' ' }.replace("[\\\\/:*?\"<>|]".toRegex(), "_")

            // Создаём папку "Tests" во внутреннем хранилище, если она ещё не существует
            val testsFolder = File(filesDir, "Tests")
            if (!testsFolder.exists()) testsFolder.mkdirs()

            // Создаём файл JSON с именем testName.json
            val testFile = File(testsFolder, "$fileName.json")

            // Записываем содержимое в файл
            try {
                FileOutputStream(testFile).use { fos ->
                    fos.write(content.toByteArray())
                    Toast.makeText(this, "Файл сохранён: $fileName.json", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } catch (e: JSONException) {
            // Если строка не является валидным JSON
            Toast.makeText(this, "Ошибка: это невалидный JSON-тест", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // Завершаем активность после сохранения (или ошибки)
        finish()
    }
}

