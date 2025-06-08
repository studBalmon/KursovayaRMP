package com.example.kursovayatesty;

import java.io.Serializable;
import java.util.List;

 /**
  * Класс Question представляет один вопрос теста
  * Реализует интерфейс Serializable для возможности сериализации (например, при передаче между активностями)
  **/
public class Question implements Serializable {

    // Текст вопроса
    private String text;

    // Список вариантов ответа
    private List<String> options;

    // Индекс правильного варианта ответа (0, 1, 2 или 3)
    private int correctIndex;

    // Индекс выбранного пользователем ответа (по умолчанию -1 — ничего не выбрано)
    // transient — не будет сериализоваться (например, при сохранении в файл или передаче в Firebase)
    private transient int selectedAnswerIndex = -1;

    // Пустой конструктор нужен для десериализации (например, при чтении из JSON)
    public Question() {
    }

    // Конструктор с параметрами
    public Question(String text, List<String> options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.selectedAnswerIndex = -1;
    }

    // Геттеры — позволяют получить значения полей
    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    // Сеттеры — позволяют изменить значения полей
    public void setText(String text) {
        this.text = text;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    // Геттер и сеттер для индекса выбранного пользователем ответа
    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }

    public void setSelectedAnswerIndex(int index) {
        this.selectedAnswerIndex = index;
    }
}

