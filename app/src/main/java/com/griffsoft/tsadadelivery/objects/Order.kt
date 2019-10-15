package com.griffsoft.tsadadelivery.objects

import com.google.firebase.Timestamp

data class Order(
    val customerContactNumber: String = "",
    val customerName: String = "",
    val datePlaced: Timestamp = Timestamp.now(),
    val lastUpdated: Timestamp = Timestamp.now(),
    val deliveryAddress: Address = Address(),
    val deliveryTimeEstimate: Int = 0,
    val id: String = "",
    val currentStage: Int = 0,
    val items: List<OrderItem> = listOf(),
    val paymentMethod: String = "",
    val restaurantAddress: String = "",
    val restaurantContactNumber: String = "",
    val restaurantImageUrl: String = "",
    val restaurantName: String = "",
    val total: Double = 0.0
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

