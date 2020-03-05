package com.griffsoft.tsadadelivery.extras

import android.content.Context
import android.os.Parcelable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.objects.Address
import com.griffsoft.tsadadelivery.objects.Restaurant
import kotlinx.android.parcel.Parcelize
import timber.log.Timber


object DistanceTimeUtil {
    private val gson = Gson()

    private fun getStoredDistanceTimes(context: Context): MutableMap<String, MutableMap<String, DistanceTime>> {
        val distanceTimesData = TDUtil.getSharedPrefsString(context, R.string.SHARED_PREFS_KEY_DISTANCE_TIMES)
        return if (distanceTimesData.isNotEmpty()) {
            val distanceTimesMapType = object : TypeToken<MutableMap<String, MutableMap<String, DistanceTime>>>() {}.type
            val map: MutableMap<String, MutableMap<String, DistanceTime>> = gson.fromJson(distanceTimesData, distanceTimesMapType)
            map
        } else {
            mutableMapOf()
        }
    }

    // Merges, saves and returns a DistanceTime dictionary for a given address
    private fun saveNewDistanceTimes(context: Context, newDistanceTimes: MutableMap<String, DistanceTime>, forAddressId: String): MutableMap<String, DistanceTime> {
        val storedDistanceTimes = getStoredDistanceTimes(context)

        if (storedDistanceTimes[forAddressId] != null) {
            // Entry already exists for address
            storedDistanceTimes[forAddressId]!!.putAll(newDistanceTimes)
        } else {
            // Create new entry for address
            storedDistanceTimes[forAddressId] = newDistanceTimes
        }

        // Save new distance times
        with (TDUtil.sharedPrefs(context).edit()) {
            val distanceTimeData = gson.toJson(storedDistanceTimes)
            putString(context.getString(R.string.SHARED_PREFS_KEY_DISTANCE_TIMES), distanceTimeData)
            apply()
        }

        return storedDistanceTimes[forAddressId]!!
    }


    fun getDistanceTimes(context: Context, restaurants: ArrayList<Restaurant>, forAddress: Address,
                         callback: (result: MutableMap<String, DistanceTime>?, error: Error?) -> Unit) {
        val selectedAddressId = forAddress.id

        val distanceTimesForCurrentAddress = getStoredDistanceTimes(context)[selectedAddressId]

        if (distanceTimesForCurrentAddress == null) {
            // No dictionary entry for this address id, must be a new address.
            Timber.d("❤️ New address detected, requesting all distanceTimes")
            return requestDistanceTimes(context, restaurants, forAddress, callback)
        }

        // Store any restaurants that don't already have a DistanceTime object
        // for the currently selected address OR have a DistanceTime object with non-OK status.
        // Will be used to construct the Distance API request later
        val missingRestaurants = restaurants.filter { (distanceTimesForCurrentAddress[it.id] == null ||
                distanceTimesForCurrentAddress[it.id]?.status != "OK") }

        if (missingRestaurants.isEmpty()) {
            return callback(distanceTimesForCurrentAddress, null)
        }

        requestDistanceTimes(context, ArrayList(missingRestaurants), forAddress, callback)
    }

    private fun requestDistanceTimes(context: Context, restaurants: ArrayList<Restaurant>, forAddress: Address,
                                     callback: (result: MutableMap<String, DistanceTime>?, error: Error?) -> Unit) {
        val geopPoints = restaurants.map { it.geoPoint }

        val url = APIRequestBuilder.getDistanceMatrixRequestUrl(geopPoints, forAddress)

//        Timber.d("❤️ Requesting distances. URL string: $url")
        url.httpGet()
            .responseJson { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Timber.e(ex, "Failed to get distancetimes, check internet...")
                    }
                    is Result.Success -> {
                        val jsonResponse = result.get().content
                        val dtMatrix = gson.fromJson(jsonResponse, DistanceTimeMatrix::class.java)

                        val newResponseDistanceTimes = processResponce(restaurants, dtMatrix)

                        if (newResponseDistanceTimes != null) {
                            callback(saveNewDistanceTimes(context, newResponseDistanceTimes, forAddress.id), null)
                        } else {
                            callback(null, null)
                        }
                    }
                }
            }
    }

    private fun processResponce(restaurants: ArrayList<Restaurant>, dtMatrix: DistanceTimeMatrix): MutableMap<String, DistanceTime>? {
        if (dtMatrix.status != "OK" || dtMatrix.rows.isEmpty()) {
            Timber.e("INCORRECT DISTANCE MATRIX RESPONSE STATUS OR EMPTY ROWS IN RESPONSE")
            return null
        }

//        val row = dtMatrix.rows.first()

        var tempDistanceTimes: ArrayList<DistanceTime> = ArrayList()

        for (i in 0 until restaurants.size) {
            tempDistanceTimes.add(DistanceTime("status", "distance", 30,"time", 12))
        }
        val distanceTimes = tempDistanceTimes

        val newDistanceTimes = mutableMapOf<String, DistanceTime>()

        restaurants.forEach {
            newDistanceTimes[it.id] = distanceTimes.removeAt(0)
        }
        return newDistanceTimes
    }
}

object APIRequestBuilder {
    fun getDistanceMatrixRequestUrl(restaurantGeoPoints: List<GeoPoint>, forAddress: Address): String {
        var request = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&"

        request += "origins=${forAddress.geoPoint.latitude},${forAddress.geoPoint.longitude}"

        request += "&destinations="

        restaurantGeoPoints.forEach {
            request += "${it.latitude}%2C${it.longitude}%7C"
        }

        request = request.removeSuffix("%7C")

        request += "&key=AIzaSyDA9qrmg1UNFPnlAZWC1Xlis5TdkNIzavM"

        return request
    }
}

@Parcelize
data class DistanceTime(
    var status: String,
    var distance: String,
    var distanceValue: Int,
    var time: String,
    var timeValue: Int
) :Parcelable

data class DistanceTimeMatrix(
    var destinationAddresses: List<String>,
    var originAddresses: List<String>,
    var rows: List<Row>,
    var status: String
) {
    data class Row(
        var elements: List<Element>
    ) {
        data class Element(
            var status: String,
            var distance: Distance,
            var duration: Duration
        ) {
            data class Distance(
                var text: String,
                var value: Int
            )

            data class Duration(
                var text: String,
                var value: Int
            )
        }
    }
}