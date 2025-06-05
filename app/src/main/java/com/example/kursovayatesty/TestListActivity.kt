package com.example.kursovayatesty

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kursovayatesty.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.io.File
import java.nio.file.Files
import java.util.Locale

class TestListActivity : AppCompatActivity() {
    private var testsListView: ListView? = null // Список локальных тестов (файлов)
    private var testsFolder: File? = null // Папка с локальными тестами

    private var cloudTestsListView: ListView? = null // Список облачных тестов (Firebase)
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применение выбранного языка и темы перед вызовом super.onCreate
        applyLanguage()
        applySelectedTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_list)

        // Инициализация элементов UI
        testsListView = findViewById(R.id.testsListView)
        testsFolder = File(filesDir, "Tests")
        if (!testsFolder!!.exists()) testsFolder!!.mkdirs() // Создаем папку, если нет


        loadTestFiles() // Загрузить локальные тесты в список
        setupBottomNav() // Настроить нижнюю навигацию

        cloudTestsListView = findViewById(R.id.cloudTestsListView)
        db = FirebaseFirestore.getInstance()

        loadCloudTests() // Загрузить облачные тесты из Firebase

        // Обработчик клика по элементу локального списка тестов
        testsListView?.setOnItemClickListener(OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
            val fileName = parent.getItemAtPosition(position) as String
            val intent = Intent(
                this@TestListActivity,
                TakeTestActivity::class.java
            )
            intent.putExtra("test_file_name", fileName) // Передаем имя файла в TakeTestActivity
            startActivity(intent)
        })
    }

    /**
     * Загрузка списка локальных тестовых файлов из папки "Tests"
     * Создает адаптер для отображения списка имен файлов в testsListView
     */
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

    /**
     * Настройка нижней навигационной панели с переходами на разные активности
     * Устанавливает текущий пункт как выбранный
     * При выборе пункта запускает соответствующую активность и закрывает текущую
     */
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

    /**
     * Адаптер для отображения списка локальных тестовых файлов
     *
     * @param items список имен файлов тестов
     */
    private inner class TestListAdapter(private val items: MutableList<String>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Создает или переиспользует View для элемента списка
         * Отображает имя файла теста и кнопки: показать QR и удалить файл
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.test_list_item, parent, false)
            }

            val fileNameView = convertView!!.findViewById<TextView>(R.id.testFileName)
            val logButton = convertView.findViewById<ImageButton>(R.id.logButton)
            val deleteButton = convertView.findViewById<ImageButton>(R.id.deleteButton)

            val fileName = items[position]
            fileNameView.text = fileName

            // Переход на TakeTestActivity при клике на элемент списка
            convertView.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@TestListActivity,
                    TakeTestActivity::class.java
                )
                intent.putExtra("test_file_name", fileName)
                startActivity(intent)
            }

            // При клике на кнопку логирования (QR) показываем QR с содержимым файла
            logButton.setOnClickListener { v: View? ->
                val file = File(testsFolder, fileName)
                if (file.exists()) {
                    try {
                        val content =
                            String(Files.readAllBytes(file.toPath()))
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

            // Удаление выбранного тестового файла с подтверждением
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

            return convertView
        }
    }

    /**
     * Загружает список облачных тестов текущего пользователя из Firestore
     * Создает адаптер и отображает список облачных тестов
     * При ошибке загрузки показывает тост с сообщением об ошибке
     */
    private fun loadCloudTests() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        db!!.collection("users")
            .document(user.uid)
            .collection("cloud_tests")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                val documents = queryDocumentSnapshots.documents
                val titles: MutableList<String?> =
                    ArrayList()
                val contents: MutableList<String?> =
                    ArrayList()

                for (doc in documents) {
                    titles.add(doc.getString("title"))
                    contents.add(doc.getString("content"))
                }

                val cleanTitles = titles.map { it ?: "" }
                val cleanContents = contents.map { it } // оставим как есть, если адаптер ожидает String?
                val adapter = CloudTestsAdapter(cleanTitles, cleanContents)

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

    /**
     * Адаптер для отображения списка облачных тестов с возможностью запуска и показа QR
     *
     * @param titles   - список заголовков тестов
     * @param contents - список JSON-содержимого тестов
     */
    private inner class CloudTestsAdapter(
        private val titles: List<String>,
        private val contents: List<String?>
    ) :
        BaseAdapter() {
        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Any {
            return titles[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Создает View для элемента списка облачных тестов
         * Отображает название теста, позволяет запускать тест и показывать QR код
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.cloud_test_item, parent, false)
            }

            val titleView = convertView!!.findViewById<TextView>(R.id.cloudTestTitle)
            val shareQrBtn = convertView.findViewById<ImageButton>(R.id.cloudShareQrButton)

            val title = titles[position]
            val content = contents[position]

            titleView.text = title

            // Запуск теста при клике на строку
            convertView.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@TestListActivity,
                    TakeTestActivity::class.java
                )
                intent.putExtra("test_content", content)
                startActivity(intent)
            }

            // Показать QR-код с содержимым теста по кнопке
            shareQrBtn.setOnClickListener { v: View? ->
                val intent = Intent(
                    this@TestListActivity,
                    ShowQrActivity::class.java
                )
                intent.putExtra("qr_content", content)
                startActivity(intent)
            }

            return convertView
        }
    }

    /**
     * Применяет выбранную пользователем тему из SharedPreferences
     */
    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!

        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

    /**
     * Применяет выбранный язык интерфейса из SharedPreferences
     * По умолчанию English, или "Русский" для русского языка
     */
    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val language = prefs.getString("language", "English")!!

        val localeCode = if (language == "Русский") "ru" else "en"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}


