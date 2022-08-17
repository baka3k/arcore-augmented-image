package com.baka3k.test.bankcardaugument.utils

import android.util.Log

object Logger {
    private const val TAG = "ARCore"

    fun d(message: String, throwable: Throwable? = null) {
        Log.d(TAG, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}