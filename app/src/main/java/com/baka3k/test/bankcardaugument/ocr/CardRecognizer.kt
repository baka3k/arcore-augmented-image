package com.baka3k.test.bankcardaugument.ocr

import android.graphics.Bitmap
import android.media.Image
import android.os.Handler
import android.view.SurfaceView
import com.baka3k.test.bankcardaugument.utils.BitmapUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CardRecognizer(private val cameraInfo: CameraInfo) {
    private val textRecognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    fun extractCardDetail(image: Image, callback: (CardDetails?) -> Unit) {
        val imageInput = InputImage.fromMediaImage(image, cameraInfo.getRotation())
        textRecognizer.process(imageInput).addOnSuccessListener {
            val text = it.text
            val cardDetails = Extractor.extractData(text)
            callback(cardDetails)
        }.addOnCompleteListener {
            image.close()
        }
    }

    private fun extractCardDetail(bitmap: Bitmap, callback: (CardDetails?) -> Unit) {
        val imageInput = InputImage.fromBitmap(bitmap, cameraInfo.getRotation())
        textRecognizer.process(imageInput).addOnSuccessListener {
            val text = it.text
            val cardDetails = Extractor.extractData(text)
            callback(cardDetails)
        }.addOnCanceledListener {
            callback(null)
        }
    }

    fun extractCardDetail(
        surfaceView: SurfaceView,
        handler: Handler,
        callback: (CardDetails?) -> Unit
    ) {
        BitmapUtils.copyPixelFromView(surfaceView, handler) { bitmap ->
            if (bitmap != null) {
                extractCardDetail(bitmap) { cardDetail ->
                    callback(cardDetail)
                }
                bitmap.recycle()
            } else {
                callback(null)
            }
        }
    }
}