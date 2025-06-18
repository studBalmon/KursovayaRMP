package com.example.kursovayatesty.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kursovayatesty.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gifView = findViewById<ImageView>(R.id.gifView)
        Glide.with(this).asGif().load(R.drawable.loading).into(gifView)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MenuActivity::class.java))
            finish()
        }, 2500)
    }
}
