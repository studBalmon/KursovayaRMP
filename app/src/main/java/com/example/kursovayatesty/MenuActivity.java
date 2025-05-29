package com.example.kursovayatesty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.buttonCreate).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateTestActivity.class));
        });

        findViewById(R.id.buttonTests).setOnClickListener(v -> {
            startActivity(new Intent(this, TestListActivity.class));
        });

        findViewById(R.id.authMenu).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        findViewById(R.id.buttonScan).setOnClickListener(v -> {
            startActivity(new Intent(this, ScanActivity.class));
        });

        findViewById(R.id.buttonSettings).setOnClickListener(v -> {
            Toast.makeText(this, "Настройки пока не реализованы", Toast.LENGTH_SHORT).show();
        });
    }
}
