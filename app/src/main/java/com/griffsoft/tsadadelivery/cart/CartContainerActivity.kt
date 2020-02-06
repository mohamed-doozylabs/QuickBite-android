package com.griffsoft.tsadadelivery.cart

import android.os.Bundle
import androidx.navigation.findNavController
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity

class CartContainerActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_container)
        supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment)

        navController.setGraph(R.navigation.nav_graph_cart)
    }
}
