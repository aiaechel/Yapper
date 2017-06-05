package com.yapper.Yapper.ui.chatrooms

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.yapper.Yapper.R
import com.yapper.Yapper.databinding.ChatroomCreationFormBinding
import com.yapper.Yapper.models.chatrooms.Chatroom


class ChatroomCreateActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {

    private val LOCATION_PERMISSION_RESULT = 107
    private val PLACE_PICKER_REQUEST = 1

    private lateinit var binding: ChatroomCreationFormBinding
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var viewmodel: ChatroomFormViewModel
    private var googleMap: GoogleMap? = null
    private var isLocationPermissionEnabled = false
    private var lastLocation : Location? = null
    private var placeResult: Place? = null
    private var mapMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.chatroom_creation_form)
        viewmodel = ViewModelProviders.of(this).get(ChatroomFormViewModel::class.java)
        binding.viewmodel = viewmodel

        setSupportActionBar(binding.chatroomCreationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        googleApiClient = with(GoogleApiClient.Builder(this)) {
            enableAutoManage(this@ChatroomCreateActivity, null)
            addConnectionCallbacks(this@ChatroomCreateActivity)
            addApi(LocationServices.API)
            addApi(Places.GEO_DATA_API)
            addApi(Places.PLACE_DETECTION_API)
            build()
        }

        googleApiClient.connect()

        binding.chatroomCreationPickPlace.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        }

        binding.chatroomCreationSubmit.setOnClickListener {
            binding.chatroomCreationSubmit.isEnabled = false
            val latlng = if (placeResult != null) {
                val prev = placeResult!!.latLng
                com.yapper.Yapper.models.chatrooms.LatLng(prev.latitude, prev.longitude)
            } else {
                com.yapper.Yapper.models.chatrooms.LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
            }
            val obj = Chatroom(location = latlng, roomName = viewmodel.roomName.get())
            val firebasePush = FirebaseDatabase.getInstance().getReference("chatrooms").push()
            obj.id = firebasePush.key
            firebasePush.setValue(obj).addOnCompleteListener {
                val intent = Intent()
                intent.putExtra("data", obj)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.chatroom_creation_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onConnectionSuspended(p0: Int) {}

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
        googleMap?.uiSettings?.setAllGesturesEnabled(false)
        // update location ui
        updateMapUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLACE_PICKER_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                placeResult = PlacePicker.getPlace(this, data)
                val placeLocation = Location("")
                if (placeResult != null) {
                    placeLocation.longitude = placeResult!!.latLng!!.longitude
                    placeLocation.latitude = placeResult!!.latLng!!.latitude
                    if (lastLocation?.distanceTo(placeLocation) ?: 0f > 5000) {
                        Toast.makeText(this, "The location you chose is too far away!", LENGTH_SHORT).show()
                        return
                    }
                }
                updateMapUI()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        isLocationPermissionEnabled = when(requestCode) {
            LOCATION_PERMISSION_RESULT -> grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            else -> false
        }
        tryGettingLocation()
    }

    private fun updateMapUI() {
        if (!checkLocationPermission()) return

        googleMap?.isMyLocationEnabled = isLocationPermissionEnabled
        googleMap?.uiSettings?.isMyLocationButtonEnabled = isLocationPermissionEnabled
        lastLocation = if (!isLocationPermissionEnabled) null else lastLocation
        tryGettingLocation()
    }

    @SuppressLint("MissingPermission")
    private fun tryGettingLocation() {
        if (isLocationPermissionEnabled) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        }

        if (lastLocation != null || placeResult != null) {
            mapMarker?.remove()
            val latlng = if (placeResult != null) {
                placeResult!!.latLng
            } else {
                LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
            }
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16f))
            mapMarker = googleMap?.addMarker(MarkerOptions().position(latlng).visible(true))
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionEnabled = true
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_RESULT)
            false
        }
    }
}

class ChatroomFormViewModel: ViewModel() {
    val roomName = ObservableField<String>("")
    val validated = ObservableBoolean(false)
    val maxCharacters = 107

    init {
        roomName.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                validated.set(validateForm())
            }
        })
    }
    fun validateForm(): Boolean {
        val trimmed = roomName.get().trim()
        return !trimmed.isEmpty() && trimmed.length <= maxCharacters
    }
}

data class ChatroomFirebaseModel(val location: com.yapper.Yapper.models.chatrooms.LatLng = com.yapper.Yapper.models.chatrooms.LatLng(0.0, 0.0),
                                 val room_name: String = "",
                                 val timestamp: Long = System.currentTimeMillis()) {

}