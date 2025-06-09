package com.example.kursovayatesty

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.util.Locale

class TakeTestActivity : AppCompatActivity() {
    private var questionsLayout: LinearLayout? = null
    private var submitButton: Button? = null
    private var questions: List<Question> = ArrayList()
    private var testTitle: String? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_test)

        questionsLayout = findViewById(R.id.questionsLayout)
        submitButton = findViewById(R.id.submitButton)

        if (intent.hasExtra("test_content")) {
            val json = intent.getStringExtra("test_content")
            Log.d("TakeTestActivity", "Loaded test from cloud/json: $json")
            loadTestFromJson(json)
        } else if (intent.hasExtra("test_file_name")) {
            val fileName = intent.getStringExtra("test_file_name")
            loadTestFromFile(fileName)
        } else {
            Toast.makeText(this, "Источник теста не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        submitButton?.setOnClickListener { checkAnswers() }

        setupBottomNav()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun loadTestFromFile(fileName: String?) {
        try {
            val file = File(filesDir, "Tests/$fileName")
            val content = String(Files.readAllBytes(file.toPath()))
            loadTestFromJson(content)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки файла", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun loadTestFromJson(json: String?) {
        try {
            val gson = Gson()
            val test = gson.fromJson(json, Test::class.java)
            this.testTitle = test.title
            this.questions = test.questions!!
            displayQuestions()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка разбора теста", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun displayQuestions() {
        val inflater = LayoutInflater.from(this)
        questionsLayout!!.removeAllViews()

        for (i in questions.indices) {
            val q = questions[i]
            val view = inflater.inflate(R.layout.item_question_take, null)

            val questionText = view.findViewById<TextView>(R.id.questionText)
            val radioGroup = view.findViewById<RadioGroup>(R.id.answersGroup)

            questionText.text = (i + 1).toString() + ". " + q.text

            for (j in q.options!!.indices) {
                val rb = RadioButton(this)
                rb.text = q.options!![j]
                rb.id = j
                radioGroup.addView(rb)
            }

            val finalI = i
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                questions[finalI].selectedAnswerIndex = checkedId
            }

            questionsLayout!!.addView(view)
        }
    }

    private fun checkAnswers() {
        var correct = 0
        var unanswered = 0

        for (q in questions) {
            if (q.selectedAnswerIndex == -1) {
                unanswered++
            } else if (q.selectedAnswerIndex == q.correctIndex) {
                correct++
            }
        }

        if (unanswered > 0) {
            Toast.makeText(this, "Ответьте на все вопросы", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, StatisticsActivity::class.java)
        intent.putExtra("correct", correct)
        intent.putExtra("testName", testTitle)
        intent.putExtra("total", questions.size)
        startActivity(intent)
        finish()
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_test
        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            when (id) {
                R.id.nav_test -> startActivity(Intent(this, TestListActivity::class.java))
                R.id.nav_create -> startActivity(Intent(this, CreateTestActivity::class.java))
                R.id.nav_menu -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_scan -> startActivity(Intent(this, ScanActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            finish()
            true
        }
    }

    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!
        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = prefs.getString("language", "English")!!
        val localeCode = if (language == "Русский") "ru" else "en"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
