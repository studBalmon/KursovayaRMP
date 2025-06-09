package com.example.kursovayatesty

import java.io.Serializable

class Test : Serializable {
    var title: String? = null
    var questions: List<Question>? = null

    constructor()

    constructor(title: String?, questions: List<Question>?) {
        this.title = title
        this.questions = questions
    }
}
