package com.yapper.Yapper.utils

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LiveData
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationListener(val activity: FragmentActivity) : LiveData<Location>(), LocationListener, GoogleApiClient.OnConnectionFailedListener {
    var enabled = false
    val googleApiClient : GoogleApiClient
    val locationRequest : LocationRequest

    init {
        googleApiClient = GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, null)
                .addApi(LocationServices.API)
                .build()
        locationRequest = LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000)
    }

    override fun onActive() {
        if (enabled && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
        }
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