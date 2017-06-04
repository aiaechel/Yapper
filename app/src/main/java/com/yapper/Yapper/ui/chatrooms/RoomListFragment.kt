package com.yapper.Yapper.ui.chatrooms

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yapper.Yapper.R

class RoomListFragment: LifecycleFragment() {

    private lateinit var recyclerview: RecyclerView
    private lateinit var viewmodel: ChatroomListViewModel
    private lateinit var listeners: ChatroomClickListeners
    private lateinit var adapter: RoomListAdapter

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listeners = context as? ChatroomClickListeners ?: BlankListeners()
        adapter = RoomListAdapter(listeners)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        recyclerview = inflater?.inflate(R.layout.chatroom_recyclerview, container, false) as RecyclerView
        recyclerview.adapter = adapter
        viewmodel = ViewModelProviders.of(activity).get(ChatroomListViewModel::class.java)
        viewmodel.getChatrooms().observe(this, Observer {
            adapter.updateItems(it)
        })
        return recyclerview
    }
}