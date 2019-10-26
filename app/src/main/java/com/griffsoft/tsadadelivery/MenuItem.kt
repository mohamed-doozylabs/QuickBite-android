package com.griffsoft.tsadadelivery

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MenuItem(
    val itemName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val featured: Boolean = false,
    val itemImageUrl: String = "",
    val itemOptionCategories: List<MenuItemOptionCategory> = listOf(),

    // Properties used when the item is added to an order
    var selectedOptions: String = "",
    var selectedQuantity: Int = 0,
    var finalPrice: Double = 0.0,
    var specialInstructions: String = ""
) : Parcelable

@Parcelize
data class MenuItemOptionCategory(
    val optionsCategoryName: String = "",
    val options: List<MenuItemOption> = listOf(),
    val singleSelection: Boolean = false,
    val required: Boolean = false
) : Parcelable

@Parcelize
data class MenuItemOption(
    val optionName: String = "",
    val addedPrice: Double = 0.0
) : Parcelable