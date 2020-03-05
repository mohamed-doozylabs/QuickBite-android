package com.griffsoft.tsadadelivery.ui.orders

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class CurrentOrderFragmentDirections private constructor() {
    companion object {
        fun actionCurrentOrderFragmentToPastOrdersFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_currentOrderFragment_to_pastOrdersFragment)
    }
}
