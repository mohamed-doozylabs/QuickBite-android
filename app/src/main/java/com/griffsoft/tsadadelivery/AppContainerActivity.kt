package com.griffsoft.tsadadelivery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.griffsoft.tsadadelivery.get_started.LoginActivity
import timber.log.Timber

class AppContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val currentUser = UserUtil.getCurrentUser(this)
        val activityIntent = if (currentUser != null && currentUser.addresses.isNotEmpty()) {
            Intent(this, TDTabBarActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(activityIntent)
        finish()
    }
}
