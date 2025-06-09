package com.example.kursovayatesty

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kursovayatesty.ScoreManager.saveBestScore
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Collections
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val exitButton = findViewById<Button>(R.id.exitButton)
        exitButton.setOnClickListener {
            val intent = Intent(this@StatisticsActivity, TestListActivity::class.java)
            startActivity(intent)
            finish()
        }

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val resultText = findViewById<TextView>(R.id.resultText)

        val testName = intent.getStringExtra("testName")
        val correct = intent.getIntExtra("correct", 0)
        val total = intent.getIntExtra("total", 1)

        val percentage = (correct.toFloat() / total) * 100
        saveBestScore(this, testName, percentage)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val scoreRef = db.collection("users").document(user.uid)
                .collection("test_scores").document(testName!!)

            scoreRef.get().addOnSuccessListener { doc: DocumentSnapshot ->
                val cloudBest = if (doc.contains("best_percent"))
                    doc.getDouble("best_percent")!!.toFloat()
                else 0f

                if (percentage > cloudBest) {
                    scoreRef.set(
                        Collections.singletonMap("best_percent", percentage)
                    )
                }
            }
        }

        resultText.text = "Правильных: $correct из $total (${percentage.toInt()}%)"

        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(correct.toFloat(), "Верные"))
        entries.add(PieEntry((total - correct).toFloat(), "Ошибки"))

        val dataSet = PieDataSet(entries, "Результаты")
        dataSet.setColors(Color.GREEN, Color.RED)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 16f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "Результат"
        pieChart.setCenterTextSize(20f)
        pieChart.holeRadius = 40f
        pieChart.description.isEnabled = false

        val legend = pieChart.legend
        legend.textSize = 14f

        pieChart.invalidate()
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
