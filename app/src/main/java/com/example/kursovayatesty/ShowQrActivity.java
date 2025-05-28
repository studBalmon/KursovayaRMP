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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        ImageView qrImage = findViewById(R.id.qrImageView);
        ImageButton closeButton = findViewById(R.id.closeButton);

        String content = getIntent().getStringExtra("qr_content");
        if (content != null) {
            QRCodeWriter writer = new QRCodeWriter();
            try {
                Bitmap bitmap = toBitmap(writer.encode(content, BarcodeFormat.QR_CODE, 800, 800));
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        closeButton.setOnClickListener(v -> finish());
    }

    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }
}
