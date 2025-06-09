package com.example.kursovayatesty

import java.io.Serializable

class Question : Serializable {
    @JvmField
    var text: String? = null

    @JvmField
    var options: List<String>? = null

    @JvmField
    var correctIndex: Int = 0

    @JvmField
    @Transient
    var selectedAnswerIndex: Int = -1

    constructor()

    constructor(text: String?, options: List<String>?, correctIndex: Int) {
        this.text = text
        this.options = options
        this.correctIndex = correctIndex
        this.selectedAnswerIndex = -1
    }
}
