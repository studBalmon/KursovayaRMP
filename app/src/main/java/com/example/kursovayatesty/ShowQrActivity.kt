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
    /**
     * Метод жизненного цикла активности.
     * Получает строку с содержимым для QR-кода из Intent.
     * Создаёт QR-код и отображает его в ImageView.
     * Также настраивает кнопку закрытия.
     *
     * @param savedInstanceState - сохранённое состояние активности (не используется)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr) // Устанавливаем разметку

        val qrImage = findViewById<ImageView>(R.id.qrImageView)
        val closeButton = findViewById<ImageButton>(R.id.closeButton)

        // Получаем текст для генерации QR-кода из переданного Intent
        val content = intent.getStringExtra("qr_content")
        if (content != null) {
            val writer = QRCodeWriter()
            try {
                // Генерируем битовую матрицу QR-кода, преобразуем в Bitmap и показываем в ImageView
                val bitmap = toBitmap(writer.encode(content, BarcodeFormat.QR_CODE, 800, 800))
                qrImage.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                e.printStackTrace() // Ошибка генерации QR-кода
            }
        }

        // Обработчик кнопки закрытия: завершает активность
        closeButton.setOnClickListener { v: View? -> finish() }
    }

    /**
     * Преобразует BitMatrix (матрицу черно-белых пикселей) в объект Bitmap.
     *
     * @param matrix - битовая матрица QR-кода, где true - черный пиксель, false - белый.
     * @return объект Bitmap с изображением QR-кода.
     */
    private fun toBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // Проходим по всем пикселям матрицы и устанавливаем цвет пикселя в Bitmap
        for (x in 0..<width) {
            for (y in 0..<height) {
                bmp.setPixel(x, y, if (matrix[x, y]) -0x1000000 else -0x1) // Черный или белый
            }
        }
        return bmp
    }
}

