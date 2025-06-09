package com.example.kursovayatesty

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

class ShowQrActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr)

        val qrImage = findViewById<ImageView>(R.id.qrImageView)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        val content = intent.getStringExtra("qr_content")
        if (content != null) {
            val writer = QRCodeWriter()
            try {
                val bitmap = toBitmap(writer.encode(content, BarcodeFormat.QR_CODE, 800, 800))
                qrImage.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        closeButton.setOnClickListener { finish() }
    }

    private fun toBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0..<width) {
            for (y in 0..<height) {
                bmp.setPixel(x, y, if (matrix[x, y]) -0x1000000 else -0x1)
            }
        }
        return bmp
    }
}
