package com.griffsoft.tsadadelivery.ui.account

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class AccountFragmentDirections private constructor() {
    companion object {
        fun actionAccountToNotificationsFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_account_to_notificationsFragment)

        fun actionAccountToAddressesFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_account_to_addressesFragment)

        fun actionAccountToAccountDetailsFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_account_to_accountDetailsFragment)
    }
}
