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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestListActivity extends AppCompatActivity {

    private ListView testsListView;
    private File testsFolder;

    private ListView cloudTestsListView;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        testsListView = findViewById(R.id.testsListView);
        testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();

        loadTestFiles();
        setupBottomNav();
        cloudTestsListView = findViewById(R.id.cloudTestsListView);
        db = FirebaseFirestore.getInstance();

        loadCloudTests();

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
            ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);  // <- новая кнопка

            String fileName = items.get(position);
            fileNameView.setText(fileName);

            // Переход при клике по имени
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
                intent.putExtra("test_file_name", fileName);
                startActivity(intent);
            });

            // Показать QR
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

            // Удаление файла
            deleteButton.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(TestListActivity.this)
                        .setTitle("Удаление теста")
                        .setMessage("Удалить файл \"" + fileName + "\"?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            File file = new File(testsFolder, fileName);
                            if (file.exists() && file.delete()) {
                                items.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(TestListActivity.this, "Файл удалён", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(TestListActivity.this, "Не удалось удалить файл", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });

            return convertView;
        }


    }
    private void loadCloudTests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .collection("cloud_tests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    List<String> titles = new ArrayList<>();
                    List<String> contents = new ArrayList<>();

                    for (DocumentSnapshot doc : documents) {
                        titles.add(doc.getString("title"));
                        contents.add(doc.getString("content"));
                    }

                    CloudTestsAdapter adapter = new CloudTestsAdapter(titles, contents);
                    cloudTestsListView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки облачных тестов", Toast.LENGTH_SHORT).show());
    }

    private class CloudTestsAdapter extends BaseAdapter {
        private final List<String> titles;
        private final List<String> contents;

        CloudTestsAdapter(List<String> titles, List<String> contents) {
            this.titles = titles;
            this.contents = contents;
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public Object getItem(int position) {
            return titles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.cloud_test_item, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.cloudTestTitle);
            ImageButton shareQrBtn = convertView.findViewById(R.id.cloudShareQrButton);

            String title = titles.get(position);
            String content = contents.get(position);

            titleView.setText(title);

            // Запуск теста по нажатию на весь элемент (строку)
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
                intent.putExtra("test_content", content);
                startActivity(intent);
            });

            // Показать QR по кнопке
            shareQrBtn.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, ShowQrActivity.class);
                intent.putExtra("qr_content", content);
                startActivity(intent);
            });

            return convertView;
        }

    }



}

