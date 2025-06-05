package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Активность для создания теста: добавление вопросов, сохранение на устройство и в облако.
 */
class CreateTestActivity : AppCompatActivity() {
    // Поля интерфейса
    private var testTitleEditText: EditText? = null // поле для ввода названия теста
    private var questionsContainer: LinearLayout? = null // контейнер для всех вопросов
    private val questionViews: MutableList<View> = ArrayList() // список всех view вопросов
    private var testsFolder: File? = null // папка, где будут храниться локальные JSON-файлы тестов

    /**
     * Метод жизненного цикла: вызывается при создании активности.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage() // применить язык из настроек
        applySelectedTheme() // применить тему из настроек
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_test)

        // Инициализация UI-элементов
        testTitleEditText = findViewById(R.id.testTitleEditText)
        questionsContainer = findViewById(R.id.questionsContainer)
        val addQuestionButton = findViewById<Button>(R.id.addQuestionButton)
        val saveTestButton = findViewById<Button>(R.id.saveTestButton)

        // Создаём папку для хранения тестов, если её нет
        testsFolder = File(filesDir, "Tests")
        if (!testsFolder!!.exists()) testsFolder!!.mkdirs()

        // Кнопка: добавить вопрос
        addQuestionButton.setOnClickListener { v: View? -> addQuestionView() }

        // Кнопка: сохранить тест в файл
        saveTestButton.setOnClickListener { v: View? -> saveTestToFile() }

        // Кнопка: сохранить тест в облако (Firebase)
        findViewById<View>(R.id.saveToCloudButton).setOnClickListener { v: View? ->
            val title = testTitle
            val questions = collectQuestions()
            saveTestToCloud(title, questions)
        }

        setupBottomNav() // навигационное меню
        addQuestionView() // сразу добавляем 1 вопрос при создании
    }

    /**
     * Добавляет один блок вопроса в интерфейс.
     * Инициализирует переключатели ответов.
     */
    private fun addQuestionView() {
        val questionView = layoutInflater.inflate(R.layout.question_item, null)
        questionsContainer!!.addView(questionView)
        questionViews.add(questionView)

        val radioGroup = questionView.findViewById<RadioGroup>(R.id.radioGroup)

        // Установка логики: при нажатии на радиокнопку она становится выбранной
        for (i in 0..3) {
            val finalI = i
            val radio = radioGroup.getChildAt(i) as RadioButton
            radio.setOnClickListener { v: View? -> radioGroup.check(radio.id) }
        }
    }

    /**
     * Сохраняет тест в файл в формате JSON.
     */
    private fun saveTestToFile() {
        val title = testTitle
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название теста", Toast.LENGTH_SHORT).show()
            return
        }

        val questions = collectQuestions()
        if (questions.isEmpty()) {
            Toast.makeText(this, "Добавьте хотя бы один вопрос", Toast.LENGTH_SHORT).show()
            return
        }

        val test = Test(title, questions)
        val gson = Gson()
        val json = gson.toJson(test)

        Log.d("CreateTestActivity", "Сериализованный JSON теста:\n$json")

        val testFile = File(testsFolder, "$title.json")
        try {
            FileOutputStream(testFile).use { fos ->
                fos.write(json.toByteArray())
                Toast.makeText(this, "Тест сохранён (JSON)", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Обработка нажатий в нижней навигации.
     */
    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_create
        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            if (id == R.id.nav_test) {
                startActivity(Intent(this, TestListActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_create) {
                startActivity(Intent(this, CreateTestActivity::class.java))
                finish()
            }
            if (id == R.id.nav_menu) {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_scan) {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_settings) {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            true
        }
    }

    /**
     * Сохраняет тест в Firebase Firestore в коллекцию cloud_tests пользователя.
     *
     * @param title        Название теста.
     * @param questionList Список вопросов.
     */
    private fun saveTestToCloud(title: String, questionList: List<Question>) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()

        val test = Test(title, questionList)
        val gson = Gson()
        val json = gson.toJson(test)

        val testMap: MutableMap<String, Any> = HashMap()
        testMap["title"] = title
        testMap["content"] = json

        db.collection("users")
            .document(userId)
            .collection("cloud_tests")
            .add(testMap)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                Toast.makeText(
                    this,
                    "Тест сохранён в облаке",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this,
                    "Ошибка при сохранении: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Собирает все введённые вопросы и их ответы с экрана.
     *
     * @return Список объектов Question.
     */
    private fun collectQuestions(): List<Question> {
        val questionList: MutableList<Question> = ArrayList()

        for (i in 0..<questionsContainer!!.childCount) {
            val questionView = questionsContainer!!.getChildAt(i)

            val questionEditText = questionView.findViewById<EditText>(R.id.questionEditText)
            val optionEdits = arrayOf(
                questionView.findViewById(R.id.answer1EditText),
                questionView.findViewById(R.id.answer2EditText),
                questionView.findViewById(R.id.answer3EditText),
                questionView.findViewById<EditText>(R.id.answer4EditText),
            )
            val radioGroup = questionView.findViewById<RadioGroup>(R.id.radioGroup)
            val checkedId = radioGroup.checkedRadioButtonId

            val options: MutableList<String> = ArrayList()
            var correctIndex = -1

            for (j in 0..3) {
                options.add(optionEdits[j].text.toString())

                val radioButton = radioGroup.getChildAt(j) as RadioButton
                if (radioButton.id == checkedId) {
                    correctIndex = j
                }
            }

            val questionText = questionEditText.text.toString().trim { it <= ' ' }

            if (!questionText.isEmpty() && options.size == 4 && correctIndex != -1) {
                questionList.add(Question(questionText, options, correctIndex))
            }
        }

        return questionList
    }

    private val testTitle: String
        /**
         * Получает название теста из поля ввода.
         *
         * @return Строка — заголовок теста.
         */
        get() = testTitleEditText!!.text.toString().trim { it <= ' ' }

    /**
     * Применяет тему, выбранную пользователем в настройках.
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
     * Применяет язык интерфейса, выбранный пользователем.
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
