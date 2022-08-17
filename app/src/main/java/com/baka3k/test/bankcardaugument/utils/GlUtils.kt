package com.baka3k.test.bankcardaugument.utils

import android.app.ActivityManager
import android.content.Context

object GlUtils {
    fun configOpenGLMode(context: Context) {
        val openGlVersionString =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Logger.e("Sceneform requires OpenGL ES 3.0 or later")
        }
    }

    private const val MIN_OPENGL_VERSION = 3.0
}