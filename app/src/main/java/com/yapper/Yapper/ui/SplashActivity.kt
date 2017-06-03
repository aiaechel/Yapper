package com.yapper.Yapper.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.yapper.Yapper.R
import com.yapper.Yapper.utils.ChatRoom


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        startActivity(Intent(this, ChatRoom::class.java))

    }

}
