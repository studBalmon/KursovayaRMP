package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.model.Question
import com.example.kursovayatesty.model.Test
import com.example.kursovayatesty.viewmodel.CreateTestViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class CreateTestActivity : AppCompatActivity() {

    private val viewModel: CreateTestViewModel by viewModels()

    private lateinit var testTitleEditText: EditText
    private lateinit var questionsContainer: LinearLayout
    private val questionViews: MutableList<View> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_test)

        testTitleEditText = findViewById(R.id.testTitleEditText)
        questionsContainer = findViewById(R.id.questionsContainer)

        val addQuestionButton = findViewById<Button>(R.id.addQuestionButton)
        val saveTestButton = findViewById<Button>(R.id.saveTestButton)
        val saveToCloudButton = findViewById<View>(R.id.saveToCloudButton)

        addQuestionButton.setOnClickListener { addQuestionView() }

        saveTestButton.setOnClickListener {
            val test = collectTest()
            if (test != null) {
                viewModel.saveTestToFile(test)
            }
        }

        saveToCloudButton.setOnClickListener {
            val test = collectTest()
            if (test != null) {
                viewModel.saveTestToCloud(test)
            }
        }

        setupBottomNav()
        addQuestionView()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.testSavedMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        viewModel.testSavedError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    private fun addQuestionView() {
        val questionView = layoutInflater.inflate(R.layout.question_item, null)
        questionsContainer.addView(questionView)
        questionViews.add(questionView)

        val radioGroup = questionView.findViewById<RadioGroup>(R.id.radioGroup)

        for (i in 0..3) {
            val radio = radioGroup.getChildAt(i) as RadioButton
            radio.setOnClickListener {
                radioGroup.check(radio.id)
            }
        }
    }

    private fun collectTest(): Test? {
        val title = testTitleEditText.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название теста", Toast.LENGTH_SHORT).show()
            return null
        }

        val questions = mutableListOf<Question>()

        for (questionView in questionViews) {
            val questionEditText = questionView.findViewById<EditText>(R.id.questionEditText)
            val optionEdits = listOf(
                questionView.findViewById<EditText>(R.id.answer1EditText),
                questionView.findViewById<EditText>(R.id.answer2EditText),
                questionView.findViewById<EditText>(R.id.answer3EditText),
                questionView.findViewById<EditText>(R.id.answer4EditText)
            )
            val radioGroup = questionView.findViewById<RadioGroup>(R.id.radioGroup)
            val checkedId = radioGroup.checkedRadioButtonId

            val options = mutableListOf<String>()
            var correctIndex = -1

            for (i in optionEdits.indices) {
                options.add(optionEdits[i].text.toString())
                val radioButton = radioGroup.getChildAt(i) as RadioButton
                if (radioButton.id == checkedId) {
                    correctIndex = i
                }
            }

            val questionText = questionEditText.text.toString().trim()

            if (questionText.isNotEmpty() && options.size == 4 && correctIndex != -1) {
                questions.add(Question(questionText, options, correctIndex))
            } else {
                Toast.makeText(this, "Проверьте правильность заполнения вопросов и ответов", Toast.LENGTH_SHORT).show()
                return null
            }
        }

        if (questions.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы один вопрос", Toast.LENGTH_SHORT).show()
            return null
        }

        return Test(title, questions)
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_create
        nav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_test -> {
                    startActivity(Intent(this, TestListActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_create -> {
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
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
