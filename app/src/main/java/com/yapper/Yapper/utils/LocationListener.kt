package com.yapper.Yapper.utils

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationListener(val googleApiClient: GoogleApiClient) : LiveData<Location>(), LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private var enabled = false

    val locationRequest : LocationRequest

    init {
        locationRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        value = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    override fun onInactive() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    override fun onLocationChanged(location: Location?) {
        value = location
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        enabled = false
    }
}