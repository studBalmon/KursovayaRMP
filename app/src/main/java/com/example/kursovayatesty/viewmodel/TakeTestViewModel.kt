package com.example.kursovayatesty.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kursovayatesty.model.Question
import com.example.kursovayatesty.model.Test
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files

class TakeTestViewModel(application: Application) : AndroidViewModel(application) {

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> = _questions

    private val _testTitle = MutableLiveData<String>()
    val testTitle: LiveData<String> = _testTitle

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadTestFromJson(json: String?) {
        if (json == null) {
            _error.value = "Тест не найден"
            return
        }
        try {
            val gson = Gson()
            val test = gson.fromJson(json, Test::class.java)
            _testTitle.value = test.title
            _questions.value = test.questions ?: emptyList()
        } catch (e: Exception) {
            _error.value = "Ошибка разбора теста"
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTestFromFile(fileName: String?) {
        if (fileName == null) {
            _error.value = "Имя файла не указано"
            return
        }
        try {
            val file = File(getApplication<Application>().filesDir, "Tests/$fileName")
            val content = String(Files.readAllBytes(file.toPath()))
            loadTestFromJson(content)
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки файла"
            e.printStackTrace()
        }
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        _questions.value = _questions.value?.mapIndexed { index, question ->
            if (index == questionIndex) question.copy(selectedAnswerIndex = answerIndex) else question
        }
    }

    fun checkAnswers(): Result {
        val questions = _questions.value ?: return Result.Error("Вопросы отсутствуют")
        var correct = 0
        var unanswered = 0

        for (q in questions) {
            if (q.selectedAnswerIndex == -1) {
                unanswered++
            } else if (q.selectedAnswerIndex == q.correctIndex) {
                correct++
            }
        }

        return if (unanswered > 0) {
            Result.Unanswered
        } else {
            Result.Success(correct, questions.size)
        }
    }

    fun clearError() {
        _error.value = null
    }

    sealed class Result {
        data class Success(val correct: Int, val total: Int) : Result()
        object Unanswered : Result()
        data class Error(val message: String) : Result()
    }
}
