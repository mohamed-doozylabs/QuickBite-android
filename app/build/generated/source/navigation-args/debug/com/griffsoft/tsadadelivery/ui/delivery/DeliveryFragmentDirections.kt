package com.griffsoft.tsadadelivery.ui.delivery

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class DeliveryFragmentDirections private constructor() {
    companion object {
        fun actionDeliveryHomeToRestaurantFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_deliveryHome_to_restaurantFragment)

        fun actionDeliveryHomeToSearchFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_deliveryHome_to_searchFragment)
    }
}
