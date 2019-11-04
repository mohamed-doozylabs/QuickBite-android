package com.griffsoft.tsadadelivery.objects

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
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return if (other is MenuItem) {
            this.itemName == other.itemName &&
                    this.description == other.description &&
                    this.selectedOptions == other.selectedOptions &&
                    this.specialInstructions == other.specialInstructions
        } else {
            false
        }
    }

    val orderDictionary: HashMap<String, Any>
        get() {
            return hashMapOf(
                "itemName" to itemName,
                "finalPrice" to finalPrice,
                "selectedQuantity" to selectedQuantity,
                "selectedOptions" to selectedOptions,
                "specialInstructions" to specialInstructions
            )
        }
}

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