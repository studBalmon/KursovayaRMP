package com.example.kursovayatesty.data

import android.content.Context
import com.example.kursovayatesty.model.CloudTest
import com.example.kursovayatesty.model.LocalTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.io.File

class TestRepository(private val context: Context) {
    private val testsFolder = File(context.filesDir, "Tests").apply { if (!exists()) mkdirs() }
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    fun getLocalTests(): List<LocalTest> {
        val files = testsFolder.listFiles()
        val tests = mutableListOf<LocalTest>()
        if (files != null) {
            for (file in files) {
                if (file.isFile) {
                    tests.add(LocalTest(file.name))
                }
            }
        }
        return tests
    }

    suspend fun getCloudTests(): List<CloudTest> {
        if (user == null) return emptyList()

        val snapshot: QuerySnapshot = db.collection("users")
            .document(user.uid)
            .collection("cloud_tests")
            .get()
            .await()

        val tests = mutableListOf<CloudTest>()
        for (doc in snapshot.documents) {
            val title = doc.getString("title") ?: continue
            val content = doc.getString("content") ?: continue
            tests.add(CloudTest(title, content))
        }
        return tests
    }

    fun deleteLocalTest(fileName: String): Boolean {
        val file = File(testsFolder, fileName)
        return file.exists() && file.delete()
    }

    suspend fun deleteCloudTest(title: String): Boolean {
        if (user == null) return false
        return try {
            db.collection("users")
                .document(user.uid)
                .collection("cloud_tests")
                .document(title)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readLocalTestContent(fileName: String): String? {
        val file = File(testsFolder, fileName)
        if (!file.exists()) return null
        return file.readText()
    }
}
