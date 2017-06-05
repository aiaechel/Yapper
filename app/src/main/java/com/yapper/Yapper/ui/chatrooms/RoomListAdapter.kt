package com.yapper.Yapper.ui.chatrooms

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yapper.Yapper.databinding.ChatroomListItemBinding
import com.yapper.Yapper.models.chatrooms.Chatroom

class RoomListAdapter(val listeners: ChatroomClickListeners): RecyclerView.Adapter<ChatroomViewHolder>() {

    private var items = ArrayList<Chatroom>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ChatroomViewHolder {
        val binding = ChatroomListItemBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        binding.listeners = listeners
        return ChatroomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun insertItem(room: Chatroom, position: Int) {
        items.add(position, room)
        notifyItemInserted(position)
    }

    fun updateItems(rooms: List<Chatroom>?) {
        items.clear()
        if (rooms != null) {
            items.addAll(rooms.sortedBy {
                (if (it.isSubscribed) "-" else "") + it.id
            })
        }
        notifyDataSetChanged()
    }
}

class ChatroomViewHolder(val chatroomBinding: ChatroomListItemBinding): RecyclerView.ViewHolder(chatroomBinding.root) {

    fun bind(item: Chatroom) {
        chatroomBinding.chatroom = item
        chatroomBinding.executePendingBindings()
    }
}

interface ChatroomClickListeners {
    fun onClicked(view: View)
    fun onStarClicked()
}

class BlankListeners : ChatroomClickListeners {
    override fun onStarClicked() {}

    override fun onClicked(view: View) {}
}