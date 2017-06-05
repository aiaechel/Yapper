package com.yapper.Yapper.network.chatrooms

import com.google.firebase.auth.FirebaseAuth
import com.yapper.Yapper.models.chatrooms.Chatroom
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetChatroomsService {
    @GET("/getNearbyChatrooms")
    fun getNearbyChatrooms(@Query("user_id") id: String = FirebaseAuth.getInstance().currentUser?.uid ?: "", @Query("lat") lat: Double, @Query("lng") lng: Double, @Query("rad") rad: Int = 5)
        : Call<List<Chatroom>>
}