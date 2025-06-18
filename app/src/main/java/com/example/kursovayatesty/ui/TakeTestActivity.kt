package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.viewmodel.TakeTestViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem
import java.util.Locale

class TakeTestActivity : AppCompatActivity() {

    private lateinit var questionsLayout: LinearLayout
    private lateinit var submitButton: Button

    private val viewModel: TakeTestViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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
            viewModel.loadTestFromJson(json)
        } else if (intent.hasExtra("test_file_name")) {
            val fileName = intent.getStringExtra("test_file_name")
            viewModel.loadTestFromFile(fileName)
        } else {
            Toast.makeText(this, "Источник теста не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        submitButton.setOnClickListener {
            when (val result = viewModel.checkAnswers()) {
                is TakeTestViewModel.Result.Unanswered -> Toast.makeText(this, "Ответьте на все вопросы", Toast.LENGTH_SHORT).show()
                is TakeTestViewModel.Result.Success -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    intent.putExtra("correct", result.correct)
                    intent.putExtra("total", result.total)
                    intent.putExtra("testName", viewModel.testTitle.value)
                    startActivity(intent)
                    finish()
                }
                is TakeTestViewModel.Result.Error -> Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.questions.observe(this, Observer { questions ->
            displayQuestions(questions)
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        })

        setupBottomNav()
    }

    private fun displayQuestions(questions: List<com.example.kursovayatesty.model.Question>) {
        val inflater = LayoutInflater.from(this)
        questionsLayout.removeAllViews()

        for ((i, q) in questions.withIndex()) {
            val view = inflater.inflate(R.layout.item_question_take, null)

            val questionText = view.findViewById<TextView>(R.id.questionText)
            val radioGroup = view.findViewById<RadioGroup>(R.id.answersGroup)

            questionText.text = "${i + 1}. ${q.text}"

            radioGroup.removeAllViews()
            for ((j, option) in q.options.withIndex()) {
                val rb = RadioButton(this)
                rb.text = option
                rb.id = j
                radioGroup.addView(rb)
            }
            if (q.selectedAnswerIndex != -1) {
                radioGroup.check(q.selectedAnswerIndex)
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.selectAnswer(i, checkedId)
            }

            questionsLayout.addView(view)
        }
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
