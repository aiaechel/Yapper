package com.yapper.Yapper.ui

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler().postDelayed({
            applicationContext?.let {
                Toast.makeText(applicationContext, "THIS IS A TEST", Toast.LENGTH_SHORT).show()
            }
        }, 2000)
    }
}
