package com.yapper.Yapper.models.chatrooms

import com.google.android.gms.maps.model.LatLng

data class LatLng(val lat : Double = 0.0, val lng : Double = 0.0) {
    fun getLatLng() = LatLng(lat, lng)
}