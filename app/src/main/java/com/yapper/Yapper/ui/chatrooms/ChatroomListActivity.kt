package com.yapper.Yapper.ui.chatrooms

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.*
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yapper.Yapper.R
import com.yapper.Yapper.databinding.ChatroomListContainerBinding
import com.yapper.Yapper.models.chatrooms.Chatroom
import com.yapper.Yapper.models.chatrooms.LatLng
import com.yapper.Yapper.network.chatrooms.GetChatroomsService
import com.yapper.Yapper.utils.ChatRoom
import com.yapper.Yapper.utils.ChatRoom.ROOM_ID_KEY
import com.yapper.Yapper.utils.LocationListener
import com.yapper.Yapper.utils.RetrofitProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatroomListActivity: LifecycleActivity(), ChatroomClickListeners by BlankListeners() {

    private val LOCATION_PERMISSION = 107
    private val LOCATION_SETTING = 5

    private val roomListFragment = RoomListFragment()

    private lateinit var binding: ChatroomListContainerBinding
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var viewModel: ChatroomListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ChatroomListContainerBinding>(this, R.layout.chatroom_list_container)
        viewModel = ViewModelProviders.of(this).get(ChatroomListViewModel::class.java)
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(LocationServices.API)
                .build()

        supportFragmentManager.beginTransaction()
                .add(R.id.chatroom_main_content, roomListFragment)
                .commit()

        checkLocationPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_SETTING && resultCode == Activity.RESULT_OK) {
            observeLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            checkLocationSettings()
        }
    }

    override fun onClicked(view: View) {
        val chatroom = view.getTag() as Chatroom
        val intent = Intent(this, ChatRoom::class.java)
        intent.putExtra(ROOM_ID_KEY, chatroom.id)
        startActivity(intent)
    }

    fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 107)
        } else {
            checkLocationSettings()
        }
    }

    fun checkLocationSettings() {
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

    fun observeLocation() {
        viewModel.getLocationListener(googleApiClient).observe(this, Observer {
            viewModel.loadChatrooms(it)
        })
    }
}

class ChatroomListViewModel: ViewModel() {
    private val chatrooms: MutableLiveData<List<Chatroom>> = MutableLiveData<List<Chatroom>>()
    private val chatroomsService: GetChatroomsService = RetrofitProvider.retrofit.create(GetChatroomsService::class.java)

    private var locationListener: LocationListener? = null

    init {
        chatrooms.value = ArrayList<Chatroom>()
    }

    fun getChatrooms(): LiveData<List<Chatroom>> {
        return chatrooms
    }

    fun setChatrooms(rooms: List<Chatroom>?) {
        chatrooms.value = rooms
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