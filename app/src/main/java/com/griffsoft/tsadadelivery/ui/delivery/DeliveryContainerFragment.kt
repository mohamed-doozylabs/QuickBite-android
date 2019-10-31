package com.griffsoft.tsadadelivery.ui.delivery


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDTabBarActivity

class DeliveryContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tdTabBarActivity = activity as TDTabBarActivity
        tdTabBarActivity.setCartBannerVisibility(View.VISIBLE)
        return inflater.inflate(R.layout.fragment_delivery_container, container, false)
    }
}
