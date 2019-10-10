package com.griffsoft.tsadadelivery

import android.os.Bundle

class AddNewAddressSearchViewActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_address_search_view)
        setResult(RC_DID_RETURN_TO_LOGIN)
    }
}
