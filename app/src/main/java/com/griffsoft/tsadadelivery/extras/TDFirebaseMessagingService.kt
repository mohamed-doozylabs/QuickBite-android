package com.griffsoft.tsadadelivery.extras

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.griffsoft.tsadadelivery.SHOW_ORDER_UPDATE_SNEAKER
import timber.log.Timber

class TDFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Timber.d("❤️👋 Refreshed token: $token")

        // Attach the token to the user so that it can be submitted with the orders
        TDUtil.updateNotificationToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val newStage = if (it.title!!.contains("preparing")) 1 else 2
            val notificationIntent = Intent(SHOW_ORDER_UPDATE_SNEAKER).apply {
                putExtra("newStage", newStage)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent)
        }
    }
}