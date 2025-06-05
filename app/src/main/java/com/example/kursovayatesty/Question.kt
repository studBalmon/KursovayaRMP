package com.example.kursovayatesty

import java.io.Serializable

// Класс Question представляет один вопрос теста
// Реализует интерфейс Serializable для возможности сериализации (например, при передаче между активностями)
class Question : Serializable {
    // Сеттеры — позволяют изменить значения полей
    // Геттеры — позволяют получить значения полей
    // Текст вопроса
    @JvmField
    var text: String? = null

    // Список вариантов ответа
    @JvmField
    var options: List<String>? = null

    // Индекс правильного варианта ответа (0, 1, 2 или 3)
    @JvmField
    var correctIndex: Int = 0

    // Геттер и сеттер для индекса выбранного пользователем ответа
    // Индекс выбранного пользователем ответа (по умолчанию -1 — ничего не выбрано)
    // transient — не будет сериализоваться (например, при сохранении в файл или передаче в Firebase)
    @JvmField
    @Transient
    var selectedAnswerIndex: Int = -1

    // Пустой конструктор нужен для десериализации (например, при чтении из JSON)
    constructor()

    // Конструктор с параметрами
    constructor(text: String?, options: List<String>?, correctIndex: Int) {
        this.text = text
        this.options = options
        this.correctIndex = correctIndex
        this.selectedAnswerIndex = -1
    }
}

