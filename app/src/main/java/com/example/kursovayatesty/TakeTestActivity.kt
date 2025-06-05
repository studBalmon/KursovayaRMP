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
import com.example.kursovayatesty.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.util.Locale

class TakeTestActivity : AppCompatActivity() {
    private var questionsLayout: LinearLayout? = null // Контейнер для вопросов (вёрстка)
    private var submitButton: Button? = null // Кнопка отправки/проверки ответов
    private var questions: List<Question> = ArrayList() // Список вопросов теста

    /**
     * Метод жизненного цикла активности.
     * Загружает настройки языка и темы,
     * затем инициализирует интерфейс,
     * загружает тест из JSON (переданный через Intent) или из файла,
     * устанавливает обработчик кнопки отправки и навигации.
     *
     * @param savedInstanceState сохранённое состояние (не используется)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_test)

        questionsLayout = findViewById(R.id.questionsLayout)
        submitButton = findViewById(R.id.submitButton)

        // Проверяем источник теста: JSON из облака или имя файла
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

        // Обработка нажатия кнопки "Отправить"
        submitButton?.setOnClickListener(View.OnClickListener { v: View? -> checkAnswers() })

        setupBottomNav()
    }

    /**
     * Загружает тест из файла по имени.
     * Читает весь файл как строку, затем парсит JSON.
     *
     * @param fileName имя файла с тестом в папке "Tests"
     */
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

    /**
     * Загружает тест из JSON-строки.
     * Парсит JSON с помощью Gson в объект Test,
     * сохраняет список вопросов и отображает их.
     *
     * @param json строка с JSON-тестом
     */
    private fun loadTestFromJson(json: String?) {
        try {
            val gson = Gson()
            val test = gson.fromJson(
                json,
                Test::class.java
            )
            this.questions = test.questions!!
            displayQuestions()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка разбора теста", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * Отображает вопросы на экране.
     * Для каждого вопроса создаёт отдельный View с текстом вопроса и вариантами ответов (RadioButton).
     * Добавляет слушатель выбора варианта, чтобы записать ответ пользователя в модель вопроса.
     */
    private fun displayQuestions() {
        val inflater = LayoutInflater.from(this)
        questionsLayout!!.removeAllViews()

        for (i in questions.indices) {
            val q = questions[i]
            val view = inflater.inflate(R.layout.item_question_take, null)

            val questionText = view.findViewById<TextView>(R.id.questionText)
            val radioGroup = view.findViewById<RadioGroup>(R.id.answersGroup)

            questionText.text = (i + 1).toString() + ". " + q.text

            // Добавляем варианты ответов в RadioGroup
            for (j in q.options!!.indices) {
                val rb = RadioButton(this)
                rb.text = q.options!![j]
                rb.id = j
                radioGroup.addView(rb)
            }

            val finalI = i
            // При выборе варианта ответа обновляем поле selectedAnswerIndex в вопросе
            radioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
                questions[finalI].selectedAnswerIndex = checkedId
            }

            questionsLayout!!.addView(view)
        }
    }

    /**
     * Проверяет ответы пользователя.
     * Подсчитывает количество правильных ответов и отсутствующих (не выбранных).
     * Если есть вопросы без ответов — выводит предупреждение.
     * Иначе показывает количество правильных ответов.
     */
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

        Toast.makeText(this, "Правильных: " + correct + " из " + questions.size, Toast.LENGTH_LONG)
            .show()
    }

    /**
     * Настраивает нижнюю навигационную панель.
     * Обрабатывает выбор пунктов меню и переключает активности.
     */
    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_test
        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            if (id == R.id.nav_test) {
                startActivity(
                    Intent(
                        this,
                        TestListActivity::class.java
                    )
                )
            } else if (id == R.id.nav_create) {
                startActivity(
                    Intent(
                        this,
                        CreateTestActivity::class.java
                    )
                )
            } else if (id == R.id.nav_menu) {
                startActivity(Intent(this, MenuActivity::class.java))
            } else if (id == R.id.nav_scan) {
                startActivity(Intent(this, ScanActivity::class.java))
            } else if (id == R.id.nav_settings) {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }
            finish()
            true
        }
    }

    /**
     * Применяет выбранную в настройках тему.
     * Считывает из SharedPreferences и вызывает setTheme.
     */
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

    /**
     * Применяет выбранный язык интерфейса.
     * Считывает язык из SharedPreferences и меняет Locale приложения.
     */
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



