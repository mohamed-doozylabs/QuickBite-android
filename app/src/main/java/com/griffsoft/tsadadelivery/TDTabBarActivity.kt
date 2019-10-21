package com.griffsoft.tsadadelivery

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_tdtab_bar.*

class TDTabBarActivity : TDActivity() {

//    private val deliveryFragment = DeliveryFragment()
//    private val ordersFragment = DashboardFragment()
//    private val accountFragment = AccountFragment()
//    private var activeFragment = deliveryFragment



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tdtab_bar)
        supportActionBar!!.hide()

//        supportFragmentManager.beginTransaction().add(R.id.main)

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
        navView.setupWithNavController(navController)
    }

    fun showLoadingCoverView(show: Boolean) {
        loadingViewLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
}
