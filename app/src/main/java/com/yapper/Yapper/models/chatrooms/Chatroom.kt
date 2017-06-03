package com.yapper.Yapper.models.chatrooms

import com.yapper.Yapper.models.messages.Message

data class Chatroom(var id: String = "", var name: String = "", var location: LatLng = LatLng(0.0, 0.0), var messages: Map<String, Message> = emptyMap()) {
    fun getMessageList() = messages.toList()
}