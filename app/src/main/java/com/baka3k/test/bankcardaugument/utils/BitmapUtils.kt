package com.baka3k.test.bankcardaugument.utils

import android.graphics.Bitmap
import android.os.Handler
import android.view.PixelCopy
import android.view.SurfaceView
object BitmapUtils{
    fun copyPixelFromView(views: SurfaceView, listenerThread: Handler, callback: (Bitmap?) -> Unit) {
        if (views.width > 0 && views.height > 0) {
            var bitmap = Bitmap.createBitmap(
                views.width,
                views.height,
                Bitmap.Config.ARGB_8888
            )
            try {
                PixelCopy.request(views, bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        Logger.e("Failed to copy ArFragment view.")
                        callback(null)
                    }
                }, listenerThread)
            } catch (e: Exception) {
                Logger.e("Failed to copy ArFragment view. $e")
                callback(null)
            }
        } else {
            Logger.e("#copyPixelFromView() width and height must be > 0 (${views.width},${views.height})")
            callback(null)
        }
    }
}
