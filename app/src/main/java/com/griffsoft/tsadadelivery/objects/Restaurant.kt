package com.griffsoft.tsadadelivery.objects

import com.google.firebase.firestore.GeoPoint
import com.griffsoft.tsadadelivery.MenuItem
import com.griffsoft.tsadadelivery.extras.DistanceTime

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val categories: String = "",
    val contactNumber: String = "",
    val alternativeContactNumber: String = "",
    val geoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val rating: Double = 0.0,
    val openHours: String = "",
    val address: String = "",
    val topPick: Boolean = false,
    val imageUrl: String = "",
    val menuItems: List<MenuItem> = listOf()
    ) {

    var distanceTime: DistanceTime? = null
}