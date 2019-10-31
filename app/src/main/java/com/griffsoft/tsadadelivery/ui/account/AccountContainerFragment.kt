package com.griffsoft.tsadadelivery.ui.account


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDTabBarActivity

class AccountContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tdTabBarActivity = activity as TDTabBarActivity
        tdTabBarActivity.setCartBannerVisibility(View.GONE)

        return inflater.inflate(R.layout.fragment_account_container, container, false)
    }


}
