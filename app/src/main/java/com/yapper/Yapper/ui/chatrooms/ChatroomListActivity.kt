package com.yapper.Yapper.ui.chatrooms

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.ViewModel
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.yapper.Yapper.ui.R;
import com.yapper.Yapper.ui.databinding.ChatroomListContainerBinding

class ChatroomListActivity : LifecycleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ChatroomListContainerBinding>(this, R.layout.chatroom_list_container)
    }
}

class ChatroomListViewModel : ViewModel() {
    
}