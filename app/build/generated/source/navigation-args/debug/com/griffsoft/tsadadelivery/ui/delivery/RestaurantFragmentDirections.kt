package com.griffsoft.tsadadelivery.ui.delivery

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.griffsoft.tsadadelivery.R

class RestaurantFragmentDirections private constructor() {
    companion object {
        fun actionRestaurantFragmentToMenuCategoryFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_restaurantFragment_to_menuCategoryFragment)
    }
}
