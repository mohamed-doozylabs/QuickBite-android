package com.griffsoft.tsadadelivery.objects

import com.google.firebase.Timestamp
import java.util.*

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
    val items: List<OrderItem> = listOf(),
    val total: Double = 0.0,
    val deliveryTimeEstimate: Int = 0,
    val paymentMethod: String = "",
    val currentStage: Int = 0
) {
    fun totalQuantity(): Int {
       return items.map { it.selectedQuantity }.sum()
    }
}

data class OrderItem(
    val finalPrice: Double = 0.0,
    val itemName: String = "",
    val selectedOptions: String = "",
    val selectedQuantity: Int = 0,
    val specialInstructions: String = ""
)

