package com.example.kursovayatesty.model

data class LocalTest(
    val fileName: String
)

data class CloudTest(
    val title: String,
    val content: String
)

data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    var selectedAnswerIndex: Int = -1
)

data class Test(
    val title: String,
    val questions: List<Question>?
)

data class LoginResult(
    val success: Boolean,
    val errorMessage: String? = null
)
