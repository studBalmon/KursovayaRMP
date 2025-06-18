package com.example.kursovayatesty.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val app = application

    fun saveTestToFile(content: String) {
        try {
            val jsonObject = JSONObject(content)
            var fileName = jsonObject.optString("title", "test_" + System.currentTimeMillis())
            fileName = fileName.trim().replace("[\\\\/:*?\"<>|]".toRegex(), "_")

            val testsFolder = File(app.filesDir, "Tests")
            if (!testsFolder.exists()) testsFolder.mkdirs()

            val testFile = File(testsFolder, "$fileName.json")

            try {
                FileOutputStream(testFile).use { fos ->
                    fos.write(content.toByteArray())
                    _message.postValue("Файл сохранён: $fileName.json")
                }
            } catch (e: IOException) {
                _message.postValue("Ошибка сохранения файла")
                e.printStackTrace()
            }
        } catch (e: JSONException) {
            _message.postValue("Ошибка: это невалидный JSON-тест")
            e.printStackTrace()
        }
    }
}
