package com.example.kursovayatesty.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.kursovayatesty.R
import com.example.kursovayatesty.ui.viewmodel.ShowQrViewModel

class ShowQrActivity : AppCompatActivity() {

    private val viewModel: ShowQrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr)

        val qrImage = findViewById<ImageView>(R.id.qrImageView)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        viewModel.qrBitmap.observe(this, Observer { bitmap ->
            if (bitmap != null) {
                qrImage.setImageBitmap(bitmap)
            }
        })

        val content = intent.getStringExtra("qr_content")
        content?.let {
            viewModel.generateQrCode(it)
        }

        closeButton.setOnClickListener { finish() }
    }
}
