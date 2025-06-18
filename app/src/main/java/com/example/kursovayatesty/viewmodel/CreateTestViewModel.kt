package com.example.kursovayatesty.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.kursovayatesty.model.Question
import com.example.kursovayatesty.model.Test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

class CreateTestViewModel(application: Application) : AndroidViewModel(application) {

    val testSavedMessage = MutableLiveData<String>()
    val testSavedError = MutableLiveData<String>()

    private val context = getApplication<Application>().applicationContext

    fun saveTestToFile(test: Test) {
        val gson = Gson()
        val json = gson.toJson(test)
        val testsFolder = File(context.filesDir, "Tests")
        if (!testsFolder.exists()) testsFolder.mkdirs()
        val testFile = File(testsFolder, "${test.title}.json")

        try {
            FileOutputStream(testFile).use { fos ->
                fos.write(json.toByteArray())
                testSavedMessage.postValue("Тест сохранён (JSON)")
            }
        } catch (e: Exception) {
            testSavedError.postValue("Ошибка при сохранении: ${e.message}")
        }
    }

    fun saveTestToCloud(test: Test) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            testSavedError.postValue("Пользователь не авторизован")
            return
        }

        val gson = Gson()
        val json = gson.toJson(test)

        val testMap = hashMapOf<String, Any>(
            "title" to test.title,
            "content" to json
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("cloud_tests")
            .add(testMap)
            .addOnSuccessListener {
                testSavedMessage.postValue("Тест сохранён в облаке")
            }
            .addOnFailureListener { e ->
                testSavedError.postValue("Ошибка при сохранении в облако: ${e.message}")
            }
    }
}
