package com.example.kursovayatesty

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {
    /**
     * Метод жизненного цикла активности.
     * Устанавливает layout с анимацией загрузки (gif),
     * запускает анимацию через Glide,
     * затем с задержкой 2.5 секунды переходит к главному экрану приложения.
     *
     * @param savedInstanceState - сохранённое состояние активности (не используется)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Установка layout с splash screen

        // Находим ImageView для показа GIF-анимации
        val gifView = findViewById<ImageView>(R.id.gifView)

        // Загружаем и проигрываем GIF из ресурсов с помощью библиотеки Glide
        Glide.with(this).asGif().load(R.drawable.loading).into(gifView)

        // Запускаем задержку на 2500 миллисекунд (2.5 секунды),
        // после которой выполняется переход на MenuActivity и закрытие SplashActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this@SplashActivity,
                    MenuActivity::class.java
                )
            )
            finish() // Закрываем SplashActivity, чтобы при возврате назад не показывать splash снова
        }, 2500)
    }
}

