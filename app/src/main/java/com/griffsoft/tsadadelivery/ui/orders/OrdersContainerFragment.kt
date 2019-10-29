package com.griffsoft.tsadadelivery.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.griffsoft.tsadadelivery.R

class OrdersContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders_container, container, false)

//        val navHostFragment: NavHostFragment = childFragmentManager.findFragmentById(R.id.nav_orders_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        val graph = navController.navInflater.inflate(R.navigation.nav_graph_orders)
//
//        if (UserUtil.getCurrentUser(context!!)!!.currentOrder != null) {
//            graph.startDestination = R.id.currentOrderFragment
//        } else {
//            graph.startDestination = R.id.pastOrdersFragment
//        }

//        navController.graph = graph
    }
}