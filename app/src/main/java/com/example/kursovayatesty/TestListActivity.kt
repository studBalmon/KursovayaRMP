package com.example.kursovayatesty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestListActivity extends AppCompatActivity {

    private ListView testsListView;       // Список локальных тестов (файлов)
    private File testsFolder;              // Папка с локальными тестами

    private ListView cloudTestsListView;  // Список облачных тестов (Firebase)
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применение выбранного языка и темы перед вызовом super.onCreate
        applyLanguage();
        applySelectedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        // Инициализация элементов UI
        testsListView = findViewById(R.id.testsListView);
        testsFolder = new File(getFilesDir(), "Tests");
        if (!testsFolder.exists()) testsFolder.mkdirs();  // Создаем папку, если нет

        loadTestFiles();
        setupBottomNav();

        cloudTestsListView = findViewById(R.id.cloudTestsListView);
        db = FirebaseFirestore.getInstance();

        loadCloudTests();

        // Обработчик клика по элементу локального списка тестов
        testsListView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = (String) parent.getItemAtPosition(position);
            Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
            intent.putExtra("test_file_name", fileName); // Передаем имя файла в TakeTestActivity
            startActivity(intent);
        });
    }

    /**
     * Загрузка списка локальных тестовых файлов из папки "Tests"
     * Создает адаптер для отображения списка имен файлов в testsListView
     */
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

    /**
     * Адаптер для отображения списка локальных тестовых файлов
     *
     * @param items список имен файлов тестов
     */
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

        /**
         * Создает или переиспользует View для элемента списка
         * Отображает имя файла теста и кнопки: показать QR и удалить файл
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.test_list_item, parent, false);
            }

            TextView fileNameView = convertView.findViewById(R.id.testFileName);
            ImageButton logButton = convertView.findViewById(R.id.logButton);
            ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);
            LinearLayout progressContainer = convertView.findViewById(R.id.progressContainer);
            View progressFill = convertView.findViewById(R.id.progressFill);

            String fileName = items.get(position);
            fileNameView.setText(fileName.split("\\.json")[0]);

            // Клик: открыть тест
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
                intent.putExtra("test_file_name", fileName);
                startActivity(intent);
            });

            // Клик: показать QR-код
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

            // Клик: удалить файл
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

            // Прогресс: сначала попытка загрузки из Firestore
            progressContainer.setVisibility(View.GONE); // по умолчанию скрываем
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Log.d("debug", fileName + fileName.split("\\.json")[0]);
                db.collection("users")
                        .document(user.getUid())
                        .collection("test_scores")
                        .document(fileName.split("\\.json")[0]) // имя файла используем как ключ
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Double bestPercent = documentSnapshot.getDouble("best_percent");
                                if (bestPercent != null) {
                                    showProgressBar(progressContainer, progressFill, bestPercent.floatValue());
                                } else {
                                    tryLoadLocalProgress(fileName, progressContainer, progressFill);
                                }
                            } else {
                                tryLoadLocalProgress(fileName, progressContainer, progressFill);
                            }
                        })
                        .addOnFailureListener(e -> {
                            tryLoadLocalProgress(fileName, progressContainer, progressFill);
                        });
            } else {
                // Если пользователь не авторизован — сразу локальный
                tryLoadLocalProgress(fileName, progressContainer, progressFill);
            }

            return convertView;
        }

        private void tryLoadLocalProgress(String fileName, LinearLayout container, View fill) {
            SharedPreferences prefs = getSharedPreferences("local_test_progress", MODE_PRIVATE);
            float percent = prefs.getFloat(fileName, -1f);
            if (percent >= 0) {
                showProgressBar(container, fill, percent);
            } else {
                container.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Загружает список облачных тестов текущего пользователя из Firestore
     * Создает адаптер и отображает список облачных тестов
     * При ошибке загрузки показывает тост с сообщением об ошибке
     */
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


    /**
     * Адаптер для отображения списка облачных тестов с возможностью запуска и показа QR
     *
     * @param titles   - список заголовков тестов
     * @param contents - список JSON-содержимого тестов
     */
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
            LinearLayout progressContainer = convertView.findViewById(R.id.progressContainer);
            View progressFill = convertView.findViewById(R.id.progressFill);
            ImageButton deleteButton = convertView.findViewById(R.id.cloudDeleteButton);

            String title = titles.get(position);
            String content = contents.get(position);

            titleView.setText(title);

            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, TakeTestActivity.class);
                intent.putExtra("test_content", content);
                startActivity(intent);
            });

            shareQrBtn.setOnClickListener(v -> {
                Intent intent = new Intent(TestListActivity.this, ShowQrActivity.class);
                intent.putExtra("qr_content", content);
                startActivity(intent);
            });

            deleteButton.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(TestListActivity.this)
                        .setTitle("Удаление облачного теста")
                        .setMessage("Удалить тест \"" + title + "\" из облака?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                // Коллекция с облачными тестами пользователя
                                CollectionReference cloudTestsRef = db.collection("users")
                                        .document(user.getUid())
                                        .collection("cloud_tests");

                                // Поиск документа с нужным title
                                cloudTestsRef.whereEqualTo("title", title)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            if (!querySnapshot.isEmpty()) {
                                                // Предполагается, что title уникален, возьмём первый документ
                                                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                                                doc.getReference().delete()
                                                        .addOnSuccessListener(unused -> {
                                                            // Также удалить best_percent
                                                            db.collection("users")
                                                                    .document(user.getUid())
                                                                    .collection("test_scores")
                                                                    .document(title)
                                                                    .delete();

                                                            titles.remove(position);
                                                            contents.remove(position);
                                                            notifyDataSetChanged();

                                                            Toast.makeText(TestListActivity.this, "Тест удалён", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(TestListActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                                                            Log.e("DELETE_CLOUD", "Ошибка удаления: ", e);
                                                        });
                                            } else {
                                                Toast.makeText(TestListActivity.this, "Тест не найден в облаке", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(TestListActivity.this, "Ошибка при поиске теста", Toast.LENGTH_SHORT).show();
                                            Log.e("DELETE_CLOUD", "Ошибка поиска: ", e);
                                        });
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });


            // Скрываем по умолчанию
            progressContainer.setVisibility(View.GONE);
            progressFill.getLayoutParams().width = 0;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(user.getUid())
                        .collection("test_scores")
                        .document(title)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Double bestPercent = documentSnapshot.getDouble("best_percent");
                                if (bestPercent != null) {
                                    showProgressBar(progressContainer, progressFill, bestPercent.floatValue());
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressContainer.setVisibility(View.GONE);
                        });
            }

            return convertView;
        }
    }

    private void showProgressBar(LinearLayout container, View fill, float percent) {
        container.setVisibility(View.VISIBLE);

        int color;
        if (percent < 50) {
            color = Color.RED;
        } else if (percent < 80) {
            color = Color.parseColor("#FFC107"); // Жёлтый
        } else {
            color = Color.parseColor("#4CAF50"); // Зелёный
        }

        fill.setBackgroundColor(color);

        // Обновляем ширину заливки на основании процента
        container.post(() -> {
            int fullWidth = container.getWidth();
            int targetWidth = (int) (fullWidth * (percent / 100f));

            ViewGroup.LayoutParams params = fill.getLayoutParams();
            params.width = targetWidth;
            fill.setLayoutParams(params);
        });
    }

    private void applySelectedTheme() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Light");

        switch (theme) {
            case "Light":
                setTheme(R.style.Theme_KursovayaTesty_Light);
                break;
            case "Dark":
                setTheme(R.style.Theme_KursovayaTesty_Dark);
                break;
            case "Special":
                setTheme(R.style.Theme_KursovayaTesty_Special);
                break;
        }
    }

    /**
     * Применяет выбранный язык интерфейса из SharedPreferences
     * По умолчанию English, или "Русский" для русского языка
     */
    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String language = prefs.getString("language", "English");

        String localeCode = language.equals("Русский") ? "ru" : "en";
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
