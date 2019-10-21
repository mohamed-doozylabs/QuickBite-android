package com.griffsoft.tsadadelivery.ui.delivery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.account.AddressesActivity
import timber.log.Timber

class DeliveryFragment : Fragment(), View.OnClickListener {

    private lateinit var deliveryViewModel: DeliveryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("❤️ onCreateView!")
//        deliveryViewModel = ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_delivery, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        deliveryViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        val addressLabel: TextView = root.findViewById(R.id.addressLabel)
        addressLabel.text = UserUtil.getCurrentUser(context!!)!!.selectedAddress.displayName

        root.findViewById<View>(R.id.deliveryHeaderBackground).setOnClickListener(this)

        return root
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.deliveryHeaderBackground -> switchAddress()
        }
    }

    private fun switchAddress() {
        val addressesIntent = Intent(activity, AddressesActivity::class.java)
        addressesIntent.putExtra("settingsMode", false)
        startActivity(addressesIntent)
    }
}