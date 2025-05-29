package com.example.kursovayatesty;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {
    private String title;
    private List<Question> questions;

    public Test() {}

    public Test(String title, List<Question> questions) {
        this.title = title;
        this.questions = questions;
    }

    public String getTitle() { return title; }
    public List<Question> getQuestions() { return questions; }

    public void setTitle(String title) { this.title = title; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
