package com.griffsoft.tsadadelivery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AppContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
