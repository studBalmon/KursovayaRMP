package com.example.kursovayatesty.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kursovayatesty.data.TestRepository
import com.example.kursovayatesty.model.CloudTest
import com.example.kursovayatesty.model.LocalTest
import kotlinx.coroutines.launch

class TestListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TestRepository(application.applicationContext)

    private val _localTests = MutableLiveData<List<LocalTest>>()
    val localTests: LiveData<List<LocalTest>> = _localTests

    private val _cloudTests = MutableLiveData<List<CloudTest>>()
    val cloudTests: LiveData<List<CloudTest>> = _cloudTests

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadLocalTests()
        loadCloudTests()
    }

    fun loadLocalTests() {
        _localTests.value = repository.getLocalTests()
    }

    fun loadCloudTests() {
        viewModelScope.launch {
            try {
                _cloudTests.value = repository.getCloudTests()
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки облачных тестов"
            }
        }
    }

    fun deleteLocalTest(fileName: String) {
        if (repository.deleteLocalTest(fileName)) {
            loadLocalTests()
        } else {
            _error.value = "Не удалось удалить файл"
        }
    }

    fun deleteCloudTest(title: String) {
        viewModelScope.launch {
            val success = repository.deleteCloudTest(title)
            if (success) {
                loadCloudTests()
            } else {
                _error.value = "Не удалось удалить тест из облака"
            }
        }
    }

    fun readLocalTestContent(fileName: String): String? {
        return repository.readLocalTestContent(fileName)
    }

    fun clearError() {
        _error.value = null
    }
}
