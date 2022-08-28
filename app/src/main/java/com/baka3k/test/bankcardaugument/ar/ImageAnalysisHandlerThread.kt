package com.baka3k.test.bankcardaugument.ar

import android.media.Image
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceView
import com.baka3k.test.bankcardaugument.ocr.CardDetails
import com.baka3k.test.bankcardaugument.ocr.CardRecognizer

class ImageAnalysisHandlerThread(private val cardRecognizer: CardRecognizer) :
    HandlerThread("ImageAnalysisHandlerThread") {

    private lateinit var ioHandler: Handler
    private var previousTime = 0L
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        ioHandler = Handler(looper)
    }

    fun scanCardInfor(image: Image, callback: (CardDetails?) -> Unit) {
        if (allowExecute()) {
            cardRecognizer.extractCardDetail(image)
            {
                callback(it)
            }
        }
    }

    private fun allowExecute(): Boolean {
        return System.currentTimeMillis() - previousTime > 1000
    }
}