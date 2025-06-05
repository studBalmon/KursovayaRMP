package com.example.kursovayatesty

import java.io.Serializable

class Test : Serializable {
    // Сеттеры
    // Геттеры
    var title: String? = null // Название теста
    var questions: List<Question>? = null // Список вопросов теста

    // Пустой конструктор (нужен для Gson и сериализации)
    constructor()

    // Конструктор с параметрами
    constructor(title: String?, questions: List<Question>?) {
        this.title = title
        this.questions = questions
    }
}
