package com.example.kursovayatesty;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {
    private String title;               // Название теста
    private List<Question> questions;   // Список вопросов теста

    // Пустой конструктор (нужен для Gson и сериализации)
    public Test() {
    }

    // Конструктор с параметрами
    public Test(String title, List<Question> questions) {
        this.title = title;
        this.questions = questions;
    }

    // Геттеры
    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    // Сеттеры
    public void setTitle(String title) {
        this.title = title;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
