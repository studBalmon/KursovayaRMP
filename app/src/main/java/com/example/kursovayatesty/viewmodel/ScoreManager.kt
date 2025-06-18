package com.example.kursovayatesty.viewmodel

import android.content.Context

object ScoreManager {
    private const val PREF_NAME = "test_scores"

    @JvmStatic
    fun saveBestScore(context: Context, testName: String?, percent: Float) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val prev = prefs.getFloat(testName, 0f)
        if (percent > prev) {
            prefs.edit().putFloat(testName, percent).apply()
        }
    }

    fun getBestScore(context: Context, testName: String?): Float {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(testName, 0f)
    }
}
