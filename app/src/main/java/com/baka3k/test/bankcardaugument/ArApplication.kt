package com.baka3k.test.bankcardaugument

import android.app.Application

class ArApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applicationInstant = this
    }

    companion object {
        lateinit var applicationInstant: ArApplication
    }
}