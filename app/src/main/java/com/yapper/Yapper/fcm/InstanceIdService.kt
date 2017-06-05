package com.yapper.Yapper.fcm

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService


class InstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val instanceId = FirebaseInstanceId.getInstance().token
        Log.d("@@@@", "onTokenRefresh: " + instanceId!!)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(firebaseUser.uid)
                    .child("instance_id")
                    .setValue(instanceId)
        }
    }
}