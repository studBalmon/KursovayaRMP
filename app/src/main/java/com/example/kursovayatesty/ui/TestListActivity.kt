package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.kursovayatesty.R
import com.example.kursovayatesty.viewmodel.TestListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class TestListActivity : AppCompatActivity() {

    private lateinit var localTestsListView: ListView
    private lateinit var cloudTestsListView: ListView

    private val viewModel: TestListViewModel by viewModels()

    private lateinit var localAdapter: TestListAdapter
    private lateinit var cloudAdapter: CloudTestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        applySelectedTheme()
        applyLanguage()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_list)
        setupBottomNav()

        localTestsListView = findViewById(R.id.testsListView)
        cloudTestsListView = findViewById(R.id.cloudTestsListView)

        localAdapter = TestListAdapter(this, mutableListOf()) { test ->
            viewModel.deleteLocalTest(test.fileName)
        }
        localTestsListView.adapter = localAdapter

        cloudAdapter = CloudTestsAdapter(this, mutableListOf()) { test ->
            viewModel.deleteCloudTest(test.title)
        }
        cloudTestsListView.adapter = cloudAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.localTests.observe(this) { localTests ->
            localAdapter.apply {
                items.clear()
                items.addAll(localTests)
                notifyDataSetChanged()
            }
        }

        viewModel.cloudTests.observe(this) { cloudTests ->
            cloudAdapter.apply {
                items.clear()
                items.addAll(cloudTests)
                notifyDataSetChanged()
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupBottomNav() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_test
        nav.setOnItemSelectedListener { item: MenuItem ->
            val id = item.itemId
            when (id) {
                R.id.nav_test -> startActivity(Intent(this, TestListActivity::class.java))
                R.id.nav_create -> startActivity(Intent(this, CreateTestActivity::class.java))
                R.id.nav_menu -> startActivity(Intent(this, MenuActivity::class.java))
                R.id.nav_scan -> startActivity(Intent(this, ScanActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            finish()
            true
        }
    }

    private fun applySelectedTheme() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light")!!
        when (theme) {
            "Light" -> setTheme(R.style.Theme_KursovayaTesty_Light)
            "Dark" -> setTheme(R.style.Theme_KursovayaTesty_Dark)
            "Special" -> setTheme(R.style.Theme_KursovayaTesty_Special)
        }
    }

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
