package com.griffsoft.tsadadelivery.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity

class ContactInfoContainerActivity : TDActivity() {

    private var name = "ERROR NAME"
    private var phone = "ERROR PHONE"

    override fun onCreate(savedInstanceState: Bundle?) {

        name = intent.getStringExtra("name")
        phone = intent.getStringExtra("phone")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_info_container)
        supportActionBar?.hide()
    }

    override fun onAttachFragment(fragment: Fragment) {
        val finalizeOrderFragment = fragment as FinalizeOrderFragment
        finalizeOrderFragment.namedPassedFromReviewAndPlace = name
        finalizeOrderFragment.phonePassedFromReviewAndPlace = phone
        super.onAttachFragment(fragment)
    }
}
