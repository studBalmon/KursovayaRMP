package com.example.kursovayatesty.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

class ShowQrViewModel : ViewModel() {

    private val _qrBitmap = MutableLiveData<Bitmap?>()
    val qrBitmap: LiveData<Bitmap?> = _qrBitmap

    fun generateQrCode(content: String, size: Int = 800) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            _qrBitmap.value = toBitmap(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            _qrBitmap.value = null
        }
    }

    private fun toBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (matrix[x, y]) -0x1000000 else -0x1)
            }
        }
        return bmp
    }
}
