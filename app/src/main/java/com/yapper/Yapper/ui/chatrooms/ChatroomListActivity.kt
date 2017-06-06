package com.yapper.Yapper.ui.chatrooms

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.*
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.yapper.Yapper.R
import com.yapper.Yapper.databinding.ChatroomListContainerBinding
import com.yapper.Yapper.models.chatrooms.Chatroom
import com.yapper.Yapper.network.chatrooms.GetChatroomsService
import com.yapper.Yapper.ui.signin.GoogleSignInActivity
import com.yapper.Yapper.utils.ChatRoom
import com.yapper.Yapper.utils.ChatRoom.ROOM_ID_KEY
import com.yapper.Yapper.utils.LocationListener
import com.yapper.Yapper.utils.RetrofitProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatroomListActivity: LifecycleActivity(), ChatroomClickListeners by BlankListeners(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    private val LOCATION_PERMISSION = 107
    private val LOCATION_SETTING = 5
    private val CHATROOM_CREATE_RESULT = 96;

    private val roomListFragment = RoomListFragment()
    private val mapFragment = SupportMapFragment()

    private lateinit var binding: ChatroomListContainerBinding
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var viewModel: ChatroomListViewModel
    private var googleMap: GoogleMap? = null
    private var mapInit = false

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_chatrooms_list, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.login_status_button -> {
//                val intent = Intent(this, GoogleSignInActivity::class.java)
//                startActivity(intent)
//                return true
//            }
//
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ChatroomListContainerBinding>(this, R.layout.chatroom_list_container)
        viewModel = ViewModelProviders.of(this).get(ChatroomListViewModel::class.java)
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(LocationServices.API)
                .build()

        binding.chatroomListFab.setOnClickListener {
            startActivityForResult(Intent(this, ChatroomCreateActivity::class.java), CHATROOM_CREATE_RESULT)
        }

        binding.chatroomListBottomnav.setOnNavigationItemSelectedListener {
            with (supportFragmentManager.beginTransaction().hide(mapFragment).hide(roomListFragment)) {
                show (when (it.itemId) {
                    R.id.chatroom_list_list -> roomListFragment
                    R.id.chatroom_list_maps -> mapFragment
                    else -> roomListFragment
                })
                commit()
                true
            }
        }

        googleApiClient.registerConnectionCallbacks(this)

        supportFragmentManager.beginTransaction()
                .add(R.id.chatroom_main_content, roomListFragment)
                .add(R.id.chatroom_main_content, mapFragment)
                .hide(mapFragment)
                .commit()

        checkLocationPermission()

        checkSignIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                LOCATION_SETTING -> observeLocation()
                CHATROOM_CREATE_RESULT -> {
                    val newRoom = data?.getParcelableExtra<Chatroom>("data")
                    if (newRoom != null) {
                        viewModel.addChatroom(newRoom)
                        openChatroom(newRoom)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            checkLocationSettings()
        }
    }

    override fun onConnectionSuspended(p0: Int) {}

    override fun onConnected(p0: Bundle?) {
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        if (!mapInit) {
            mapInit = true
            googleMap?.setOnInfoWindowClickListener {
                val chatroom = it.tag as? Chatroom
                chatroom?.let {
                    openChatroom(chatroom)
                }
            }

            viewModel.getChatrooms().observe(this, Observer {
                if (it != null) {
                    addChatroomMarkers(it)
                }
            })
            viewModel.getLocationListener(googleApiClient).observe(this, Observer {
                moveMapLocation(it)
            })
        }
    }

    override fun onClicked(view: View) {
        val chatroom = view.getTag() as Chatroom
        openChatroom(chatroom)
    }

    private fun openChatroom(room: Chatroom) {
        val intent = Intent(this, ChatRoom::class.java)
        intent.putExtra(ROOM_ID_KEY, room.id)
        startActivity(intent)
    }

    private fun checkSignIn() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(this, GoogleSignInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 107)
        } else {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(viewModel.getLocationListener(googleApiClient).locationRequest)
                .build()
        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest).setResultCallback {
            when (it.status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> observeLocation()
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> it.status.startResolutionForResult(this, 5)
            }
        }
    }

    private fun observeLocation() {
        viewModel.getLocationListener(googleApiClient).observe(this, Observer {
            viewModel.loadChatrooms(it)
        })
    }

    @SuppressLint("MissingPermission")
    private fun moveMapLocation(location: Location?) {
        if (location != null) {
            val latlng = GoogleLatLng(location.latitude, location.longitude)
            googleMap?.isMyLocationEnabled = true
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
            viewModel.userMarker?.remove()
            viewModel.userMarker = googleMap?.addMarker(with (MarkerOptions()) {
                position(latlng)
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                title("Your Location")
            })
        }
    }

    private fun addChatroomMarkers(chatrooms: List<Chatroom>) {
        viewModel.chatroomMarkers.forEach {
            it.remove()
            viewModel.chatroomMarkers.remove(it)
        }
        chatrooms.forEach { chatroom ->
            googleMap?.addMarker(with (MarkerOptions()) {
                position(chatroom.location.asGoogleLatLng())
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                title(chatroom.roomName)
            })?.let {
                it.tag = chatroom
                viewModel.chatroomMarkers.add(it)
            }
        }

    }
}

class ChatroomListViewModel: ViewModel() {
    private val chatrooms: MutableLiveData<List<Chatroom>> = MutableLiveData<List<Chatroom>>()
    private val chatroomsService: GetChatroomsService = RetrofitProvider.retrofit.create(GetChatroomsService::class.java)

    private var locationListener: LocationListener? = null
    var userMarker: Marker? = null
    var chatroomMarkers: MutableSet<Marker> = mutableSetOf()

    init {
        chatrooms.value = ArrayList<Chatroom>()
    }

    fun getChatrooms(): LiveData<List<Chatroom>> {
        return chatrooms
    }

    fun setChatrooms(rooms: List<Chatroom>?) {
        chatrooms.value = rooms
    }

    fun addChatroom(room: Chatroom) {
        chatrooms.value = listOf(room) + (chatrooms.value ?: emptyList())
    }

    fun getLocationListener(googleApiClient: GoogleApiClient): LocationListener {
        if (locationListener == null) {
            locationListener = LocationListener(googleApiClient)
        }
        return locationListener!!
    }

    fun loadChatrooms(location: Location?) {
        location ?: return
        // TODO: not default radius of 5
        chatroomsService.getNearbyChatrooms(location.latitude, location.longitude, 5).enqueue(object : Callback<List<Chatroom>> {

            override fun onResponse(call: Call<List<Chatroom>>?, response: Response<List<Chatroom>>?) {
                if (response?.isSuccessful ?: false) {
                    setChatrooms(response?.body())
                }
            }
            override fun onFailure(call: Call<List<Chatroom>>?, t: Throwable?) {

            }
        })
    }
}