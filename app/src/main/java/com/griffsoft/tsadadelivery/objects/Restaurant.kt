package com.griffsoft.tsadadelivery.objects

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.griffsoft.tsadadelivery.extras.DistanceTime
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val categories: String = "",
    val contactNumber: String = "",
    val alternativeContactNumber: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Double = 0.0,
    val openHours: String = "",
    val address: String = "",
    val topPick: Boolean = false,
    val imageUrl: String = "",
    val menuItems: List<MenuItem> = listOf(),
    var distanceTime: DistanceTime? = null
    ) : Parcelable {

    @IgnoredOnParcel
    val geoPoint: GeoPoint = GeoPoint(latitude, longitude)

    fun getFeaturedItems(): ArrayList<MenuItem> {
        return ArrayList(menuItems.filter { it.featured })
    }

    // TODO: This is going to need to be reworked slightly when items have more than one category
    fun getMenuCategories(): ArrayList<String> {
        val categories = arrayListOf<String>()

        menuItems.forEach {
            if (!categories.contains(it.category)) {
                categories.add(it.category)
            }
        }
        categories.sort()
        return categories
    }

    fun getItemsInCategory(category: String) : ArrayList<MenuItem> {
        return ArrayList(menuItems.filter { it.category == category })
    }
}