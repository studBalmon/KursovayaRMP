package com.example.kursovayatesty.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kursovayatesty.ui.SettingsActivity.Companion.KEY_LANGUAGE
import com.example.kursovayatesty.ui.SettingsActivity.Companion.KEY_THEME
import com.example.kursovayatesty.ui.SettingsActivity.Companion.PREFS_NAME

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadSettings() {
        val lang = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
        val th = prefs.getString(KEY_THEME, "Light") ?: "Light"
        _language.postValue(lang)
        _theme.postValue(th)
    }

    fun saveSettings(language: String, theme: String) {
        prefs.edit()
            .putString(KEY_LANGUAGE, language)
            .putString(KEY_THEME, theme)
            .apply()
        _message.postValue("Настройки сохранены")
        _language.postValue(language)
        _theme.postValue(theme)
    }
}
