package com.yapper.Yapper.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.yapper.Yapper.R
import com.yapper.Yapper.ui.chatrooms.ChatroomListActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        Handler().postDelayed({
            applicationContext?.let {
                Toast.makeText(applicationContext, "THIS IS A TEST", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SplashActivity, ChatroomListActivity::class.java)
                startActivity(intent)
            }
        }, 2000)
    }
}
