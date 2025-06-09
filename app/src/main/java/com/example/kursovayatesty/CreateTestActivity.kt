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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class CreateTestActivity : AppCompatActivity() {
    private var testTitleEditText: EditText? = null
    private var questionsContainer: LinearLayout? = null
    private val questionViews: MutableList<View> = ArrayList()
    private var testsFolder: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_test)

        testTitleEditText = findViewById(R.id.testTitleEditText)
        questionsContainer = findViewById(R.id.questionsContainer)
        val addQuestionButton = findViewById<Button>(R.id.addQuestionButton)
        val saveTestButton = findViewById<Button>(R.id.saveTestButton)

        testsFolder = File(filesDir, "Tests")
        if (!testsFolder!!.exists()) testsFolder!!.mkdirs()

        addQuestionButton.setOnClickListener { addQuestionView() }
        saveTestButton.setOnClickListener { saveTestToFile() }

        findViewById<View>(R.id.saveToCloudButton).setOnClickListener {
            val title = testTitle
            val questions = collectQuestions()
            saveTestToCloud(title, questions)
        }

        setupBottomNav()
        addQuestionView()
    }

    private fun addQuestionView() {
        val questionView = layoutInflater.inflate(R.layout.question_item, null)
        questionsContainer!!.addView(questionView)
        questionViews.add(questionView)

        val radioGroup = questionView.findViewById<RadioGroup>(R.id.radioGroup)

        for (i in 0..3) {
            val radio = radioGroup.getChildAt(i) as RadioButton
            radio.setOnClickListener { radioGroup.check(radio.id) }
        }
    }

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
                    startActivity(Intent(this, CreateTestActivity::class.java))
                    finish()
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
            .addOnSuccessListener {
                Toast.makeText(this, "Тест сохранён в облаке", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка при сохранении: " + e.message, Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun collectQuestions(): List<Question> {
        val questionList: MutableList<Question> = ArrayList()

        for (i in 0 until questionsContainer!!.childCount) {
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

            val questionText = questionEditText.text.toString().trim()

            if (questionText.isNotEmpty() && options.size == 4 && correctIndex != -1) {
                questionList.add(Question(questionText, options, correctIndex))
            }
        }

        return questionList
    }

    private val testTitle: String
        get() = testTitleEditText!!.text.toString().trim()

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
