package com.example.kursovayatesty;

import android.content.Context;
import android.content.SharedPreferences;


public class ScoreManager {
    private static final String PREF_NAME = "test_scores";

    public static void saveBestScore(Context context, String testName, float percent) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        float prev = prefs.getFloat(testName, 0);
        if (percent > prev) {
            prefs.edit().putFloat(testName, percent).apply();
        }
    }

    public static float getBestScore(Context context, String testName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(testName, 0);
    }
}
