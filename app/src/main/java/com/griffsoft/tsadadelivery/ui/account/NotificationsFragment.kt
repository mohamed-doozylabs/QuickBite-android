package com.griffsoft.tsadadelivery.ui.account


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.UserUtil

class NotificationsFragment : TDFragment() {
    private lateinit var pushSwitch: Switch
    private lateinit var smsSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        setupBackButton(root)

        pushSwitch = root.findViewById(R.id.pushSwitch)
        smsSwitch = root.findViewById(R.id.smsSwitch)

        val user = UserUtil.getCurrentUser(context!!)!!

        pushSwitch.isChecked = user.pushNotificationsEnabled
        smsSwitch.isChecked = user.smsNotificationsEnabled

        return root
    }

    override fun onDestroy() {
        UserUtil.setPushNotificationsEnabled(context!!, pushSwitch.isChecked)
        UserUtil.setSmsNotificationsEnabled(context!!, smsSwitch.isChecked)

        super.onDestroy()
    }

}
