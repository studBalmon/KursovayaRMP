package com.example.kursovayatesty.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val prefs = application.getSharedPreferences("app_settings", Application.MODE_PRIVATE)

    fun loadSettings() {
        val savedTheme = prefs.getString("theme", "Light") ?: "Light"
        val savedLanguage = prefs.getString("language", "English") ?: "English"
        _theme.value = savedTheme
        _language.value = savedLanguage
    }
}
