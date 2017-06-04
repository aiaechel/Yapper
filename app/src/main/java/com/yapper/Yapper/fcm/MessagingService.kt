package com.yapper.Yapper.fcm

import android.support.v7.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yapper.Yapper.R
import android.app.NotificationManager
import android.content.Context
import java.util.Date

class MessagingService : FirebaseMessagingService() {

    private val TAG = "MessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        val title = remoteMessage.notification.title
        val body = remoteMessage.notification.body

        val mBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)
                .setContentText(body)

        // Generate unique ID
        val mNotificationId = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
        // Gets an instance of the NotificationManager service
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotifyMgr.notify(mNotificationId, mBuilder.build())
    }

}
