package com.yapper.Yapper.ui.chatrooms

import android.arch.lifecycle.*
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.*
import com.yapper.Yapper.R
import com.yapper.Yapper.databinding.ChatroomListContainerBinding
import com.yapper.Yapper.models.chatrooms.Chatroom

class ChatroomListActivity : LifecycleActivity() {

    lateinit var binding : ChatroomListContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ChatroomListContainerBinding>(this, R.layout.chatroom_list_container)

        val viewModel = ViewModelProviders.of(this).get(ChatroomListViewModel::class.java)
        viewModel.getChatrooms().observe(this, Observer {
            Log.d("TESTING", it.toString())
        })
    }
}

class ChatroomListViewModel : ViewModel() {
    private val chatrooms : MutableLiveData<List<Chatroom>> = MutableLiveData<List<Chatroom>>()

    fun getChatrooms() : LiveData<List<Chatroom>> {
        chatrooms.value ?: loadChatrooms()
        return chatrooms
    }

    fun loadChatrooms() {
        val db = FirebaseDatabase.getInstance()
        db.getReference().addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot?) {
                //val test = data?.getValue(Chatroom::class.java)
                val rooms = ArrayList<Chatroom>()
                data?.child("chatrooms")?.children?.forEach {
                    val room = it.getValue(Chatroom::class.java)
                    room.id = it.key
                    rooms += room
                }
                chatrooms.value = rooms
                Log.d("TESTING", "pls")
            }

            override fun onCancelled(p0: DatabaseError?) {
                Log.d("TESTING", "WTF")
            }
        })
    }
}