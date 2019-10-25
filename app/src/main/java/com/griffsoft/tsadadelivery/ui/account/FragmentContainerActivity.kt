package com.griffsoft.tsadadelivery.ui.account

import android.os.Bundle
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity

class FragmentContainerActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        supportActionBar?.hide()
    }
}
