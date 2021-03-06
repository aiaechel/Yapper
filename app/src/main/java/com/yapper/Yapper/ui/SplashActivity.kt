package com.yapper.Yapper.ui

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.yapper.Yapper.R
import com.yapper.Yapper.databinding.SplashScreenBinding
import com.yapper.Yapper.ui.chatrooms.ChatroomListActivity
import com.yapper.Yapper.ui.signin.GoogleSignInActivity
import com.yapper.Yapper.utils.ChatRoom


class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<SplashScreenBinding>(this, R.layout.splash_screen)


        binding.chatroomListButton.setOnClickListener {
            startActivity(Intent(this, ChatroomListActivity::class.java))
        }
        binding.chatroomButton.setOnClickListener {
            startActivity(Intent(this, ChatRoom::class.java))
        }
        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, GoogleSignInActivity::class.java))
        }
    }

}
