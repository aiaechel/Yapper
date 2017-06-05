package com.yapper.Yapper.models.chatrooms

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import com.yapper.Yapper.models.messages.Message

data class Chatroom(@get:Exclude var id: String = "",
                    @SerializedName("room_name") @get:Exclude @set:Exclude var roomName: String = "",
                    var location: LatLng = LatLng(0.0, 0.0),
                    var timestamp: Long = System.currentTimeMillis(),
                    var messages: Map<String, Message> = emptyMap()) : Parcelable {
    fun getMessageList() = messages.toList()
    @PropertyName("room_name") fun getRoomNameFirebase() = roomName

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Chatroom> = object : Parcelable.Creator<Chatroom> {
            override fun createFromParcel(source: Parcel): Chatroom = Chatroom(source)
            override fun newArray(size: Int): Array<Chatroom?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readParcelable<LatLng>(LatLng::class.java.classLoader),
        source.readLong()
    ) {
        source.readMap(messages, Map::class.java.classLoader)
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(roomName)
        dest.writeParcelable(location, 0)
        dest.writeLong(timestamp)
        dest.writeMap(messages)
    }
}