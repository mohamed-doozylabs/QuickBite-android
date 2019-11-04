package com.griffsoft.tsadadelivery.cart

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.objects.MenuItem
import com.griffsoft.tsadadelivery.objects.Restaurant

object Cart {
    private val gson = Gson()

    var paymentMethod: PaymentMethod? = null

    fun getItems(context: Context): ArrayList<MenuItem> {
        val cartData = TDUtil.getSharedPrefsString(context,
            R.string.SHARED_PREFS_KEY_CART
        )
        return if (cartData.isNotEmpty()) {
            val cartType = object : TypeToken<ArrayList<MenuItem>>() {}.type
            gson.fromJson(cartData, cartType)
        } else {
            arrayListOf()
        }
    }

    fun getRestaurant(context: Context): Restaurant? {
        val restaurantData = TDUtil.getSharedPrefsString(context,
            R.string.SHARED_PREFS_KEY_CART_RESTAURANT
        )
        return if (restaurantData.isNotEmpty()) {
            gson.fromJson(restaurantData, Restaurant::class.java)
        } else {
            null
        }
    }

    fun setRestaurant(context: Context, restaurant: Restaurant) {
        with (TDUtil.sharedPrefs(context).edit()) {
            val restaurantData = gson.toJson(restaurant)
            putString(context.getString(R.string.SHARED_PREFS_KEY_CART_RESTAURANT), restaurantData)
            apply()
        }
    }

    fun getTotalPrice(context: Context): Double {
        val items = getItems(context)

        return if (items.isNotEmpty()) {
            items.map { it.finalPrice }.reduce { acc, d -> acc + d }
        } else {
            0.0
        }
    }

    fun getTotalQuantity(context: Context): Int {
        val items = getItems(context)

        return if (items.isNotEmpty()) {
            items.map { it.selectedQuantity }.reduce { acc, i -> acc + i }
        } else {
            0
        }
    }

    fun addItem(context: Context, item: MenuItem) {
        val cartItems = getItems(context)

        // Merge identical items
        var itemIsUnique = true
        cartItems.forEach { menuItem ->
            if (item.equals(menuItem)) {
                menuItem.selectedQuantity += item.selectedQuantity
                menuItem.finalPrice += item.finalPrice
                itemIsUnique = false
            }
        }

        if (itemIsUnique) {
            cartItems.add(item)
        }

        with (TDUtil.sharedPrefs(context).edit()) {
            val newCartItemsData = gson.toJson(cartItems)
            putString(context.getString(R.string.SHARED_PREFS_KEY_CART), newCartItemsData)
            apply()
        }
    }

    fun removeItemAt(context: Context, index: Int) {
        val cartItems = getItems(context)
        cartItems.removeAt(index)

        if (cartItems.isEmpty()) {
            with (TDUtil.sharedPrefs(context).edit()) {
                remove(context.getString(R.string.SHARED_PREFS_KEY_CART_RESTAURANT))
                remove(context.getString(R.string.SHARED_PREFS_KEY_CART))
                apply()
            }
        } else {
            with (TDUtil.sharedPrefs(context).edit()) {
                val newCartItemsData = gson.toJson(cartItems)
                putString(context.getString(R.string.SHARED_PREFS_KEY_CART), newCartItemsData)
                apply()
            }
        }
    }

    fun empty(context: Context) {
        with (TDUtil.sharedPrefs(context).edit()) {
            remove(context.getString(R.string.SHARED_PREFS_KEY_CART_RESTAURANT))
            remove(context.getString(R.string.SHARED_PREFS_KEY_CART))
            apply()
        }
    }
}