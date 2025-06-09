package com.example.kursovayatesty

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kursovayatesty.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.io.File
import java.nio.file.Files
import java.util.Locale

class TestListActivity : AppCompatActivity() {
    private var testsListView: ListView? = null
    private var testsFolder: File? = null

    private var cloudTestsListView: ListView? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        applySelectedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_list)

        testsListView = findViewById(R.id.testsListView)
        testsFolder = File(filesDir, "Tests")
        if (!testsFolder!!.exists()) testsFolder!!.mkdirs()

        loadTestFiles()
        setupBottomNav()

        cloudTestsListView = findViewById(R.id.cloudTestsListView)
        db = FirebaseFirestore.getInstance()

        loadCloudTests()

        testsListView?.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
            val fileName = parent.getItemAtPosition(position) as String
            val intent = Intent(
                this@TestListActivity,
                TakeTestActivity::class.java
            )
            intent.putExtra("test_file_name", fileName)
            startActivity(intent)
        })
    }

    private fun loadTestFiles() {
        val files = testsFolder!!.listFiles()
        val fileNames: MutableList<String> = ArrayList()
        if (files != null) {
            for (file in files) {
                if (file.isFile) fileNames.add(file.name)
            }
        }

        val adapter = TestListAdapter(fileNames)
        testsListView!!.adapter = adapter
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_test
        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            if (id == R.id.nav_test) {
                startActivity(
                    Intent(
                        this,
                        TestListActivity::class.java
                    )
                )
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_create) {
                startActivity(
                    Intent(
                        this,
                        CreateTestActivity::class.java
                    )
                )
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_menu) {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_scan) {
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
                return@setOnItemSelectedListener true
            }
            if (id == R.id.nav_settings) {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
                finish()
                return@setOnItemSelectedListener true
            }
            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private inner class TestListAdapter(private val items: MutableList<String>) : BaseAdapter() {
        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.test_list_item, parent, false)
            }

            val fileNameView = convertView!!.findViewById<TextView>(R.id.testFileName)
            val logButton = convertView.findViewById<ImageButton>(R.id.logButton)
            val deleteButton = convertView.findViewById<ImageButton>(R.id.deleteButton)
            val progressContainer = convertView.findViewById<LinearLayout>(R.id.progressContainer)
            val progressFill = convertView.findViewById<View>(R.id.progressFill)

            val fileName = items[position]
            fileNameView.text = fileName.split("\\.json".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

            convertView.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@TestListActivity,
                    TakeTestActivity::class.java
                )
                intent.putExtra("test_file_name", fileName)
                startActivity(intent)
            }

            logButton.setOnClickListener { v: View? ->
                val file = File(testsFolder, fileName)
                if (file.exists()) {
                    try {
                        val content = String(Files.readAllBytes(file.toPath()))
                        val intent = Intent(
                            this@TestListActivity,
                            ShowQrActivity::class.java
                        )
                        intent.putExtra("qr_content", content)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("QR_ERROR", "Ошибка чтения файла", e)
                    }
                }
            }

            deleteButton.setOnClickListener { v: View? ->
                AlertDialog.Builder(this@TestListActivity)
                    .setTitle("Удаление теста")
                    .setMessage("Удалить файл \"$fileName\"?")
                    .setPositiveButton("Да") { dialog: DialogInterface?, which: Int ->
                        val file = File(testsFolder, fileName)
                        if (file.exists() && file.delete()) {
                            items.removeAt(position)
                            notifyDataSetChanged()
                            Toast.makeText(
                                this@TestListActivity,
                                "Файл удалён",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@TestListActivity,
                                "Не удалось удалить файл",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }

            progressContainer.visibility = View.GONE
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = FirebaseFirestore.getInstance()
                Log.d(
                    "debug",
                    fileName + fileName.split("\\.json".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0])
                db.collection("users")
                    .document(user.uid)
                    .collection("test_scores")
                    .document(fileName.split("\\.json".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0])
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val bestPercent = documentSnapshot.getDouble("best_percent")
                            if (bestPercent != null) {
                                showProgressBar(
                                    progressContainer,
                                    progressFill,
                                    bestPercent.toFloat()
                                )
                            } else {
                                tryLoadLocalProgress(fileName, progressContainer, progressFill)
                            }
                        } else {
                            tryLoadLocalProgress(fileName, progressContainer, progressFill)
                        }
                    }
                    .addOnFailureListener { e: Exception? ->
                        tryLoadLocalProgress(fileName, progressContainer, progressFill)
                    }
            } else {
                tryLoadLocalProgress(fileName, progressContainer, progressFill)
            }

            return convertView
        }

        fun tryLoadLocalProgress(fileName: String?, container: LinearLayout, fill: View) {
            val prefs = getSharedPreferences("local_test_progress", MODE_PRIVATE)
            val percent = prefs.getFloat(fileName, -1f)
            if (percent >= 0) {
                showProgressBar(container, fill, percent)
            } else {
                container.visibility = View.GONE
            }
        }
    }

    private fun loadCloudTests() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        db!!.collection("users")
            .document(user.uid)
            .collection("cloud_tests")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                val documents = queryDocumentSnapshots.documents
                val titles = mutableListOf<String>()
                val contents = mutableListOf<String?>()

                for (doc in documents) {
                    titles.add(doc.getString("title") ?: "")
                    contents.add(doc.getString("content") ?: "")
                }

                val adapter = CloudTestsAdapter(titles, contents)
                cloudTestsListView!!.adapter = adapter
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    this,
                    "Ошибка загрузки облачных тестов",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private inner class CloudTestsAdapter(
        private val titles: MutableList<String>,
        private val contents: MutableList<String?>
    ) : BaseAdapter() {
        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Any {
            return titles[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.cloud_test_item, parent, false)
            }

            val titleView = convertView!!.findViewById<TextView>(R.id.cloudTestTitle)
            val shareQrBtn = convertView.findViewById<ImageButton>(R.id.cloudShareQrButton)
            val progressContainer = convertView.findViewById<LinearLayout>(R.id.progressContainer)
            val progressFill = convertView.findViewById<View>(R.id.progressFill)
            val deleteButton = convertView.findViewById<ImageButton>(R.id.cloudDeleteButton)

            val title = titles[position]
            val content = contents[position]

            titleView.text = title

            convertView.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@TestListActivity,
                    TakeTestActivity::class.java
                )
                intent.putExtra("test_content", content)
                startActivity(intent)
            }

            shareQrBtn.setOnClickListener { v: View? ->
                if (content != null) {
                    val intent = Intent(
                        this@TestListActivity,
                        ShowQrActivity::class.java
                    )
                    intent.putExtra("qr_content", content)
                    startActivity(intent)
                }
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && content != null) {
                db!!.collection("users")
                    .document(user.uid)
                    .collection("test_scores")
                    .document(title)
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val bestPercent = documentSnapshot.getDouble("best_percent")
                            if (bestPercent != null) {
                                showProgressBar(
                                    progressContainer,
                                    progressFill,
                                    bestPercent.toFloat()
                                )
                            } else {
                                progressContainer.visibility = View.GONE
                            }
                        } else {
                            progressContainer.visibility = View.GONE
                        }
                    }
                    .addOnFailureListener { e: Exception? ->
                        progressContainer.visibility = View.GONE
                    }
            } else {
                progressContainer.visibility = View.GONE
            }

            deleteButton.setOnClickListener { v: View? ->
                AlertDialog.Builder(this@TestListActivity)
                    .setTitle("Удаление облачного теста")
                    .setMessage("Удалить тест \"$title\" из облака?")
                    .setPositiveButton("Да") { dialog: DialogInterface?, which: Int ->
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            db!!.collection("users")
                                .document(user.uid)
                                .collection("cloud_tests")
                                .document(title)
                                .delete()
                                .addOnSuccessListener {
                                    titles.removeAt(position)
                                    contents.removeAt(position)
                                    notifyDataSetChanged()
                                    Toast.makeText(
                                        this@TestListActivity,
                                        "Тест удалён из облака",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e: Exception? ->
                                    Toast.makeText(
                                        this@TestListActivity,
                                        "Не удалось удалить тест из облака",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }

            return convertView
        }
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

        val params = fill.layoutParams
        params.width = (container.width * clampedPercent / 100).toInt()
        fill.layoutParams = params
    }

    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val themeName = prefs.getString("theme", "Default")
        when (themeName) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            else -> setTheme(R.style.Theme_KursovayaTesty)
        }
    }

    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val language = prefs.getString("language", "ru")
        val locale = Locale(language!!)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
