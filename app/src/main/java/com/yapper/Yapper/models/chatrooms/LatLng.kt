package com.yapper.Yapper.models.chatrooms

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

data class LatLng(val lat: Double = 0.0, val lng: Double = 0.0) : Parcelable {
    fun asGoogleLatLng() = GoogleLatLng(lat, lng)

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<LatLng> = object : Parcelable.Creator<LatLng> {
            override fun createFromParcel(source: Parcel): LatLng = LatLng(source)
            override fun newArray(size: Int): Array<LatLng?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        source.readDouble(),
        source.readDouble()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(lat)
        dest.writeDouble(lng)
    }
}