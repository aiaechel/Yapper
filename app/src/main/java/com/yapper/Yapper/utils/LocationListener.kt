package com.yapper.Yapper.utils

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationListener(val googleApiClient: GoogleApiClient): LiveData<Location>(), LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private var enabled = false

    val locationRequest: LocationRequest

    init {
        locationRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(60000)
                .setFastestInterval(5000)
                .setSmallestDisplacement(100f)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        if (googleApiClient.isConnected) {
            if (value == null) {
                value = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
        } else {
            googleApiClient.registerConnectionCallbacks(this)
        }
    }

    override fun onInactive() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
    }

    override fun onLocationChanged(location: Location?) {
        value = location
    }

    override fun onConnected(p0: Bundle?) {
        googleApiClient.unregisterConnectionCallbacks(this)
        onActive()
    }

    override fun onConnectionSuspended(p0: Int) {}

    override fun onConnectionFailed(p0: ConnectionResult) {
        enabled = false
    }
}