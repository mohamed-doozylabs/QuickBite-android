package com.griffsoft.tsadadelivery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class TDActivity : AppCompatActivity() {

    val TAG = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.elevation = 0f
        title = ""
    }
}
