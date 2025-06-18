package com.example.kursovayatesty.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.kursovayatesty.R
import com.example.kursovayatesty.model.CloudTest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CloudTestsAdapter(
    private val context: Context,
    public val items: MutableList<CloudTest>,
    private val onDeleteClick: (CloudTest) -> Unit
) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): CloudTest = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.cloud_test_item, parent, false)

        val titleView = view.findViewById<TextView>(R.id.cloudTestTitle)
        val shareQrBtn = view.findViewById<ImageButton>(R.id.cloudShareQrButton)
        val progressContainer = view.findViewById<LinearLayout>(R.id.progressContainer)
        val progressFill = view.findViewById<View>(R.id.progressFill)
        val deleteButton = view.findViewById<ImageButton>(R.id.cloudDeleteButton)

        val test = items[position]
        titleView.text = test.title

        view.setOnClickListener {
            val intent = Intent(context, TakeTestActivity::class.java)
            intent.putExtra("test_content", test.content)
            context.startActivity(intent)
        }

        shareQrBtn.setOnClickListener {
            val intent = Intent(context, ShowQrActivity::class.java)
            intent.putExtra("qr_content", test.content)
            context.startActivity(intent)
        }

        progressContainer.visibility = View.GONE
        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .collection("test_scores")
                .document(test.title)
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

        deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Удаление облачного теста")
                .setMessage("Удалить тест \"${test.title}\" из облака?")
                .setPositiveButton("Да") { _, _ -> onDeleteClick(test) }
                .setNegativeButton("Отмена", null)
                .show()
        }

        return view
    }

    private fun showProgressBar(container: LinearLayout, fill: View, percent: Float) {
        container.visibility = View.VISIBLE
        val clampedPercent = percent.coerceIn(0f, 100f)
        val color = when {
            clampedPercent >= 75f -> Color.parseColor("#4CAF50")   // зелёный
            clampedPercent >= 50f -> Color.parseColor("#FFC107")   // янтарный
            clampedPercent >= 25f -> Color.parseColor("#FF9800")   // оранжевый
            else -> Color.parseColor("#F44336")                     // красный
        }
        fill.setBackgroundColor(color)

        container.post {
            val params = fill.layoutParams
            params.width = (container.width * clampedPercent / 100).toInt()
            fill.layoutParams = params
        }
    }
}
