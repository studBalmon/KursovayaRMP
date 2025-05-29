package com.example.kursovayatesty;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private String text;
    private List<String> options;
    private int correctIndex;

    // Поле для выбранного пользователем ответа, по умолчанию -1 (не выбран)
    private transient int selectedAnswerIndex = -1;

    public Question() {}

    public Question(String text, List<String> options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.selectedAnswerIndex = -1;
    }

    // геттеры и сеттеры
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }

    public void setText(String text) { this.text = text; }
    public void setOptions(List<String> options) { this.options = options; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public void setSelectedAnswerIndex(int index) { this.selectedAnswerIndex = index; }
}
