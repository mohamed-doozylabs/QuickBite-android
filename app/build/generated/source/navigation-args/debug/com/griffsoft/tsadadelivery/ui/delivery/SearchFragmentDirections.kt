package com.griffsoft.tsadadelivery.ui.delivery

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class SearchFragmentDirections private constructor() {
    companion object {
        fun actionSearchFragmentToRestaurantFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_searchFragment_to_restaurantFragment)
    }
}
