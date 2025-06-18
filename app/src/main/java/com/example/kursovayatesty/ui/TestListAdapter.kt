package com.example.kursovayatesty.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.kursovayatesty.R
import com.example.kursovayatesty.model.LocalTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TestListAdapter(
    private val context: Context,
    public val items: MutableList<LocalTest>,
    private val onDeleteClick: (LocalTest) -> Unit
) : BaseAdapter() {

    private val testsFolder = context.filesDir.resolve("Tests")
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): LocalTest = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.test_list_item, parent, false)

        val fileNameView = view.findViewById<TextView>(R.id.testFileName)
        val logButton = view.findViewById<ImageButton>(R.id.logButton)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
        val progressContainer = view.findViewById<LinearLayout>(R.id.progressContainer)
        val progressFill = view.findViewById<View>(R.id.progressFill)

        val test = items[position]
        fileNameView.text = test.fileName.removeSuffix(".json")

        view.setOnClickListener {
            val intent = Intent(context, TakeTestActivity::class.java)
            intent.putExtra("test_file_name", test.fileName)
            context.startActivity(intent)
        }

        logButton.setOnClickListener {
            val file = testsFolder.resolve(test.fileName)
            if (file.exists()) {
                try {
                    val content = file.readText()
                    val intent = Intent(context, ShowQrActivity::class.java)
                    intent.putExtra("qr_content", content)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка чтения файла", Toast.LENGTH_SHORT).show()
                }
            }
        }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление теста")
                .setMessage("Удалить файл \"${test.fileName}\"?")
                .setPositiveButton("Да") { _, _ -> onDeleteClick(test) }
                .setNegativeButton("Отмена", null)
                .show()
        }

        progressContainer.visibility = View.GONE
        if (user != null) {
            val docName = test.fileName.removeSuffix(".json")
            db.collection("users")
                .document(user.uid)
                .collection("test_scores")
                .document(docName)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val bestPercent = doc.getDouble("best_percent")
                        if (bestPercent != null) {
                            showProgressBar(progressContainer, progressFill, bestPercent.toFloat())
                        }
                    }
                }
                .addOnFailureListener {
                    progressContainer.visibility = View.GONE
                }
        }

        return view
    }

    private fun showProgressBar(container: LinearLayout, fill: View, percent: Float) {
        container.visibility = View.VISIBLE
        val clampedPercent = percent.coerceIn(0f, 100f)
        val color = when {
            clampedPercent >= 75f -> Color.parseColor("#4CAF50")
            clampedPercent >= 50f -> Color.parseColor("#FFC107")
            clampedPercent >= 25f -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }
        fill.setBackgroundColor(color)
        container.post {
            val params = fill.layoutParams
            params.width = (container.width * clampedPercent / 100).toInt()
            fill.layoutParams = params
        }
    }
}
