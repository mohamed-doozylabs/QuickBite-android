package com.griffsoft.tsadadelivery.objects

import com.google.firebase.firestore.GeoPoint
import java.util.*

data class Address(
    val id: String = UUID.randomUUID().toString(),
    val buildingLandmark: String = "",
    val floorDoorUnitNo: String = "",
    val instructions: String = "",
    val geoPoint: GeoPoint = GeoPoint(0.0, 0.0),
    val street: String = "",
    val userNickname: String = "",
    var selected: Boolean = false
) {
    val displayName: String
        get() = if (userNickname.isNotEmpty()) userNickname else street
}