package com.griffsoft.tsadadelivery.cart

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class FinalizeOrderFragmentDirections private constructor() {
    companion object {
        fun actionFinalizeOrderFragmentToReviewAndPlaceOrderFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_finalizeOrderFragment_to_reviewAndPlaceOrderFragment)
    }
}
