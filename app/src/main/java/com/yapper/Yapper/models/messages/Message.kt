package com.yapper.Yapper.models.messages

import org.joda.time.DateTime

data class Message(var user: String = "", var timestamp: String = "", val body: String = "") {
    fun datetime() = DateTime.parse(timestamp)
}