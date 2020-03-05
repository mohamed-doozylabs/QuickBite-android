package com.griffsoft.tsadadelivery.ui.orders

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class PastOrdersFragmentDirections private constructor() {
    companion object {
        fun actionPastOrdersFragmentToCurrentOrderFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_pastOrdersFragment_to_currentOrderFragment)
    }
}
