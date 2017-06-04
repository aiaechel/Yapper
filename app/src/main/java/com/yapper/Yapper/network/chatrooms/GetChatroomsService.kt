package com.yapper.Yapper.network.chatrooms

import retrofit2.http.GET
import retrofit2.http.Query

interface GetChatroomsService {
    @GET("/getNearbyChatrooms")
    fun getNearbyChatrooms(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("rad") rad: Int)
}