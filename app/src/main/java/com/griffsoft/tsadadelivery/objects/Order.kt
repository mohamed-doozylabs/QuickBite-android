package com.griffsoft.tsadadelivery.objects

import com.google.firebase.Timestamp
import com.griffsoft.tsadadelivery.MenuItem
import java.util.*
import kotlin.collections.ArrayList

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val customerName: String = "",
    val customerContactNumber: String = "",
    val deliveryAddress: Address = Address(),
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantContactNumber: String = "",
    val restaurantImageUrl: String = "",
    val datePlaced: Timestamp = Timestamp.now(),
    val lastUpdated: Timestamp = Timestamp.now(),
    val items: List<MenuItem> = listOf(),
    val total: Double = 0.0,
    val deliveryTimeEstimate: Int = 0,
    val paymentMethod: String = "",
    val currentStage: Int = 0
) {
    fun totalQuantity(): Int {
       return items.map { it.selectedQuantity }.sum()
    }

    val dictionary: HashMap<String, Any>
        get() {
            return hashMapOf(
                "id" to id,
                "customerName" to customerName,
                "customerContactNumber" to customerContactNumber,
                "datePlaced" to datePlaced,
                "lastUpdated" to lastUpdated,
                "deliveryAddress" to deliveryAddress,
                "restaurantName" to restaurantName,
                "restaurantAddress" to restaurantAddress,
                "restaurantContactNumber" to restaurantContactNumber,
                "restaurantImageUrl" to restaurantImageUrl,
                "items" to itemsDictionary,
                "deliveryTimeEstimate" to deliveryTimeEstimate,
                "paymentMethod" to paymentMethod,
                "total" to total,
                "currentStage" to currentStage
            )
        }

    private val itemsDictionary: ArrayList<HashMap<String, Any>>
        get() {
            return ArrayList(items.map { it.orderDictionary })
        }
}
