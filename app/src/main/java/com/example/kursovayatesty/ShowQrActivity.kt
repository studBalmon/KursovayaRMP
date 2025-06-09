package com.example.kursovayatesty;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class ShowQrActivity extends AppCompatActivity {

    /**
     * Метод жизненного цикла активности.
     * Получает строку с содержимым для QR-кода из Intent.
     * Создаёт QR-код и отображает его в ImageView.
     * Также настраивает кнопку закрытия.
     *
     * @param savedInstanceState - сохранённое состояние активности (не используется)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr); // Устанавливаем разметку

        ImageView qrImage = findViewById(R.id.qrImageView);
        ImageButton closeButton = findViewById(R.id.closeButton);

        // Получаем текст для генерации QR-кода из переданного Intent
        String content = getIntent().getStringExtra("qr_content");
        if (content != null) {
            QRCodeWriter writer = new QRCodeWriter();
            try {
                // Генерируем битовую матрицу QR-кода, преобразуем в Bitmap и показываем в ImageView
                Bitmap bitmap = toBitmap(writer.encode(content, BarcodeFormat.QR_CODE, 800, 800));
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace(); // Ошибка генерации QR-кода
            }
        }

        // Обработчик кнопки закрытия: завершает активность
        closeButton.setOnClickListener(v -> finish());
    }

    /**
     * Преобразует BitMatrix (матрицу черно-белых пикселей) в объект Bitmap.
     *
     * @param matrix - битовая матрица QR-кода, где true - черный пиксель, false - белый.
     * @return объект Bitmap с изображением QR-кода.
     */
    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        // Проходим по всем пикселям матрицы и устанавливаем цвет пикселя в Bitmap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Черный или белый
            }
        }
        return bmp;
    }
}
