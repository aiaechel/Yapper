package com.yapper.Yapper

import android.app.Application
import android.content.Context

class YapperApplication: Application() {

    companion object {

        private lateinit var context: Context

        fun getApplicationContext() = context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}