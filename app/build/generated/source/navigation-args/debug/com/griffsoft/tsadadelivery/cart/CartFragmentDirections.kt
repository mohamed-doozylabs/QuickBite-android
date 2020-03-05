package com.griffsoft.tsadadelivery.cart

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class CartFragmentDirections private constructor() {
    companion object {
        fun actionCartFragmentToFinalizeOrderFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_cartFragment_to_finalizeOrderFragment)

        fun actionCartFragmentToReviewAndPlaceOrderFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_cartFragment_to_reviewAndPlaceOrderFragment)
    }
}
