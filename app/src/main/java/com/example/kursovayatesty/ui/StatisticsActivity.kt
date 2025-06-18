package com.example.kursovayatesty.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.ui.viewmodel.StatisticsViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet

import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private val viewModel: StatisticsViewModel by viewModels()

    private lateinit var pieChart: PieChart
    private lateinit var resultText: TextView
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pieChart)
        resultText = findViewById(R.id.resultText)
        exitButton = findViewById(R.id.exitButton)

        exitButton.setOnClickListener {
            startActivity(Intent(this, TestListActivity::class.java))
            finish()
        }

        viewModel.resultText.observe(this, Observer { text ->
            resultText.text = text
        })

        viewModel.pieEntries.observe(this, Observer { entries ->
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
        })

        val testName = intent.getStringExtra("testName") ?: return
        val correct = intent.getIntExtra("correct", 0)
        val total = intent.getIntExtra("total", 1)

        viewModel.loadStatistics(testName, correct, total)
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
