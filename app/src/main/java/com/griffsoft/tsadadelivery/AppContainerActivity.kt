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

        val activityIntent: Intent

        // go straight to main if a token is stored
        if (false) {
            activityIntent = Intent(this, MainActivity::class.java)
        } else {
            activityIntent = Intent(this, LoginActivity::class.java)
        }

        startActivity(activityIntent)
        finish()
    }
}
