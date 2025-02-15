package com.griffsoft.tsadadelivery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.griffsoft.tsadadelivery.get_started.LoginActivity
import timber.log.Timber

class AppContainerActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = getString(R.string.td_channel_name)
//            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(getString(R.string.td_channel_id), name, importance)
//            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        if (Timber.treeCount() == 0 && BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (isTaskRoot) {
            Timber.d("❤️ task is root")
            val currentUser = UserUtil.getCurrentUser(this)
            val activityIntent = if (currentUser != null && currentUser.addresses.isNotEmpty()) {
                Intent(this, TDTabBarActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            if (intent.extras != null) {
                activityIntent.putExtra("redirectToOrders", true)
            }

            startActivity(activityIntent)
        } else if (intent.extras != null) {
            Timber.d("❤️ app was opened from notification")
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(REDIRECT_TO_ORDERS_ACTION))
        }

        finish()
    }
}
