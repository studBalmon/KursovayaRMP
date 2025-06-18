package com.example.kursovayatesty.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AccountSettingsViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUser: FirebaseUser? = auth.currentUser

    private val _updateEmailResult = MutableLiveData<Result<Unit>>()
    val updateEmailResult: LiveData<Result<Unit>> = _updateEmailResult

    private val _updatePasswordResult = MutableLiveData<Result<Unit>>()
    val updatePasswordResult: LiveData<Result<Unit>> = _updatePasswordResult

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    fun updateEmail(newEmail: String) {
        val user = currentUser
        if (user == null) {
            _updateEmailResult.value = Result.failure(Exception("Пользователь не авторизован"))
            return
        }

        user.updateEmail(newEmail)
            .addOnSuccessListener { _updateEmailResult.value = Result.success(Unit) }
            .addOnFailureListener { e -> _updateEmailResult.value = Result.failure(e) }
    }

    fun updatePassword(newPassword: String) {
        val user = currentUser
        if (user == null) {
            _updatePasswordResult.value = Result.failure(Exception("Пользователь не авторизован"))
            return
        }

        user.updatePassword(newPassword)
            .addOnSuccessListener { _updatePasswordResult.value = Result.success(Unit) }
            .addOnFailureListener { e -> _updatePasswordResult.value = Result.failure(e) }
    }

    fun logout() {
        auth.signOut()
        _logoutEvent.value = true
    }
}
