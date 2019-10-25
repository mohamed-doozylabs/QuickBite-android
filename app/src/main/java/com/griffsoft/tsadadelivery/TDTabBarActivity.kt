package com.griffsoft.tsadadelivery

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.plusAssign
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.griffsoft.tsadadelivery.extras.KeepStateNavigator
import kotlinx.android.synthetic.main.activity_tdtab_bar.*

class TDTabBarActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tdtab_bar)
        supportActionBar!!.hide()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_delivery, R.id.navigation_orders, R.id.navigationAccount
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)

        // get fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!

        // setup custom navigator
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider += navigator

        // set navigation graph
        navController.setGraph(R.navigation.nav_graph)

        navView.setupWithNavController(navController)
    }

    fun showLoadingCoverView(show: Boolean) {
        loadingViewLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
}
