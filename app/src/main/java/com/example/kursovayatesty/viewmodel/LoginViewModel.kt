package com.example.kursovayatesty.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kursovayatesty.model.LoginResult
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registerResult = MutableLiveData<LoginResult>()
    val registerResult: LiveData<LoginResult> = _registerResult

    fun checkUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult(false, "Заполните все поля")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = LoginResult(true)
                } else {
                    _loginResult.value = LoginResult(false, task.exception?.message)
                }
            }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _registerResult.value = LoginResult(false, "Заполните все поля")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registerResult.value = LoginResult(true)
                } else {
                    _registerResult.value = LoginResult(false, task.exception?.message)
                }
            }
    }
}
