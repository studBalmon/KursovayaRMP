package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestListActivity extends AppCompatActivity {

    private ListView testsListView;
    private File testsFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        testsListView = findViewById(R.id.testsListView);
        testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();

        loadTestFiles();
        setupBottomNav();
        testsListView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = (String) parent.getItemAtPosition(position);
            Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
            intent.putExtra("test_file_name", fileName);
            startActivity(intent);
        });
    }

    private void loadTestFiles() {
        File[] files = testsFolder.listFiles();
        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) fileNames.add(file.getName());
            }
        }

        TestListAdapter adapter = new TestListAdapter(fileNames);
        testsListView.setAdapter(adapter);
    }


    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_test);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_test) {
                startActivity(new Intent(this, TestListActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_create) {
                startActivity(new Intent(this, CreateTestActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_scan) {
                startActivity(new Intent(this, ScanActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            Toast.makeText(this, "платформа 9 3/4", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private class TestListAdapter extends BaseAdapter {
        private final List<String> items;

        TestListAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.test_list_item, parent, false);
            }

            TextView fileNameView = convertView.findViewById(R.id.testFileName);
            ImageButton logButton = convertView.findViewById(R.id.logButton);

            String fileName = items.get(position);
            fileNameView.setText(fileName);

            // Переход при клике по имени
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
                intent.putExtra("test_file_name", fileName);
                startActivity(intent);
            });

            // Лог содержимого
            logButton.setOnClickListener(v -> {
                File file = new File(testsFolder, fileName);
                if (file.exists()) {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                        Intent intent = new Intent(TestListActivity.this, ShowQrActivity.class);
                        intent.putExtra("qr_content", content);
                        startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("QR_ERROR", "Ошибка чтения файла", e);
                    }
                }
            });


            return convertView;
        }
    }

}

