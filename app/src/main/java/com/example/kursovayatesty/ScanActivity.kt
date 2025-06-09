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

class ScanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IntentIntegrator(this)
            .setPrompt("Отсканируйте QR-код теста")
            .setBeepEnabled(true)
            .setOrientationLocked(true)
            .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                saveTestToFile(result.contents)
            } else {
                Toast.makeText(this, "Сканирование отменено", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveTestToFile(content: String) {
        try {
            val jsonObject = JSONObject(content)
            var fileName = jsonObject.optString("title", "test_" + System.currentTimeMillis())
            fileName = fileName.trim { it <= ' ' }.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val testsFolder = File(filesDir, "Tests")
            if (!testsFolder.exists()) testsFolder.mkdirs()
            val testFile = File(testsFolder, "$fileName.json")

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
            Toast.makeText(this, "Ошибка: это невалидный JSON-тест", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        finish()
    }
}
