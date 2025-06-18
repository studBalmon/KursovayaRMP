package com.example.kursovayatesty.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kursovayatesty.viewmodel.ScoreManager
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Collections

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _resultText = MutableLiveData<String>()
    val resultText: LiveData<String> = _resultText

    private val _pieEntries = MutableLiveData<List<PieEntry>>()
    val pieEntries: LiveData<List<PieEntry>> = _pieEntries

    fun loadStatistics(testName: String, correct: Int, total: Int) {
        val percentage = if (total > 0) (correct.toFloat() / total) * 100 else 0f

        ScoreManager.saveBestScore(getApplication(), testName, percentage)

        _resultText.value = "Правильных: $correct из $total (${percentage.toInt()}%)"

        val entries = listOf(
            PieEntry(correct.toFloat(), "Верные"),
            PieEntry((total - correct).toFloat(), "Ошибки")
        )
        _pieEntries.value = entries

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val scoreRef = db.collection("users").document(user.uid)
            .collection("test_scores").document(testName)

        scoreRef.get().addOnSuccessListener { doc ->
            val cloudBest = if (doc.contains("best_percent"))
                doc.getDouble("best_percent")?.toFloat() ?: 0f
            else 0f

            if (percentage > cloudBest) {
                scoreRef.set(Collections.singletonMap("best_percent", percentage))
            }
        }
    }
}
