package com.griffsoft.tsadadelivery

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.findNavController
import androidx.navigation.plusAssign
import androidx.navigation.ui.setupWithNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.griffsoft.tsadadelivery.cart.Cart
import com.griffsoft.tsadadelivery.cart.CartContainerActivity
import com.griffsoft.tsadadelivery.extras.KeepStateNavigator
import kotlinx.android.synthetic.main.activity_tdtab_bar.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent


const val RC_CART = 666
const val RC_REDIRECT_TO_ORDERS = 667

@SuppressLint("SetTextI18n")
class TDTabBarActivity : TDActivity() {

    private lateinit var navView: BottomNavigationView

    private var cartBannerIsShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tdtab_bar)
        supportActionBar?.hide()

        originalConstraintSet.clone(container)

        navView = findViewById(R.id.nav_view)

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

        cartBanner.setOnClickListener {
            startActivityForResult(Intent(this, CartContainerActivity::class.java), RC_CART)
        }

        setupKeyboardListener()
    }

    override fun onResume() {
        super.onResume()
        showCartBanner()

    }

    fun showLoadingCoverView(show: Boolean) {
        loadingViewLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupKeyboardListener() {
        val hiddenNavBarConstraintSet = ConstraintSet().apply {
            clone(container)
            clear(R.id.nav_view, ConstraintSet.BOTTOM)
            connect(R.id.nav_view, ConstraintSet.TOP, R.id.container, ConstraintSet.BOTTOM)
        }

        val transition = ChangeBounds().apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 0
        }

        KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
            //            navView.visibility = if (isOpen) View.GONE else View.VISIBLE
//            val newScale = if (isOpen) 0f else 1f
//            navView
//                .animate()
//                .scaleY(newScale)
//                .setDuration(500)
//                .start()
            TransitionManager.beginDelayedTransition(container, transition)
            if (isOpen) {
                hiddenNavBarConstraintSet.applyTo(container)
            } else {
                originalConstraintSet.applyTo(container)
            }
        }
    }

    private fun showCartBanner() {
        if (Cart.getItems(this).isNotEmpty()) {
            updateCartBannerLabels()

            if (!cartBannerIsShown) {
                val altConstraintSet = ConstraintSet().apply {
                    clone(container)
                    clear(R.id.cartBanner, ConstraintSet.TOP)
                    connect(R.id.cartBanner, ConstraintSet.BOTTOM, R.id.nav_view, ConstraintSet.TOP, 8.px)
                }

                val transition = ChangeBounds().apply {
                    interpolator = AnticipateOvershootInterpolator(1.0f)
                    startDelay = 400
                    duration = 400
                }

                TransitionManager.beginDelayedTransition(container, transition)
                altConstraintSet.applyTo(container)
                cartBanner.translationZ = 4f
                cartBannerIsShown = true
            }
        } else {
            // Hide cart
            cartBanner.translationZ = 0f
            originalConstraintSet.applyTo(container)
            cartBannerIsShown = false
        }
    }

    private fun updateCartBannerLabels() {
        val quantity = Cart.getTotalQuantity(this)
        itemsLabel.text = "$quantity item${if (quantity > 1) "s" else ""}"

        cartPrice.text = Cart.getTotalPrice(this).asPriceString
    }

    fun setCartBannerVisibility(visibility: Int) {
        cartBanner.visibility = visibility
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CART) {
            if (resultCode == RC_REDIRECT_TO_ORDERS) {
                navView.selectedItemId = R.id.navigation_orders
            }
        }
    }
}
