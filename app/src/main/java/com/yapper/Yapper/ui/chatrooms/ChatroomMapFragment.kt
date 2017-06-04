package com.yapper.Yapper.ui.chatrooms

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yapper.Yapper.R

class ChatroomMapFragment: LifecycleFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.splash_screen, container, false)
    }
}