package com.griffsoft.tsadadelivery

data class MenuItem(
    val itemName: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var category: String = "",
    var featured: Boolean = false,
    var imageUrl: String = "",
//    var itemOptionCategories: [MenuItemOptionCategory] = ,

    // Properties used when the item is added to an order
    var selectedOptions: String = "",
    var selectedQuantity: Int = 0,
    var finalPrice: Double = 0.0,
    var specialInstructions: String = ""
)