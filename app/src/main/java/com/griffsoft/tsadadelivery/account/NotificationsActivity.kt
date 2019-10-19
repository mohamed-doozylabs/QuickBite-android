package com.griffsoft.tsadadelivery.account

import android.os.Bundle
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity
import com.griffsoft.tsadadelivery.UserUtil
import kotlinx.android.synthetic.main.activity_notifications.*

class NotificationsActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = UserUtil.getCurrentUser(this)!!

        pushSwitch.isChecked = user.pushNotificationsEnabled
        smsSwitch.isChecked = user.smsNotificationsEnabled
    }

    override fun onBackPressed() {
        UserUtil.setPushNotificationsEnabled(this, pushSwitch.isChecked)
        UserUtil.setSmsNotificationsEnabled(this, smsSwitch.isChecked)
        super.onBackPressed()
    }
}
