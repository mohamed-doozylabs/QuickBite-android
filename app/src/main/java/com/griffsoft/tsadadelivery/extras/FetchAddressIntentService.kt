package com.griffsoft.tsadadelivery.extras

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.android.gms.maps.model.LatLng
import com.griffsoft.tsadadelivery.R
import timber.log.Timber
import java.io.IOException
import java.util.*

class FetchAddressIntentService: IntentService("FetchAddressIntentService") {
    private var receiver: ResultReceiver? = null

    object Constants {
        const val SUCCESS_RESULT = 0
        const val GEOCODING_FAILED_RESULT = 1
        const val NO_INTERNET_RESULT = 2
        const val PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress"
        const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
        const val RESULT_DATA_KEY = "${PACKAGE_NAME}.RESULT_DATA_KEY"
        const val LOCATION_DATA_EXTRA = "${PACKAGE_NAME}.LOCATION_DATA_EXTRA"
    }

    override fun onHandleIntent(intent: Intent?) {
        // Get the location passed to this service through an extra.
        val location = intent!!.getParcelableExtra(
            Constants.LOCATION_DATA_EXTRA) as LatLng

        receiver = intent.getParcelableExtra(Constants.RECEIVER)

        val geocoder = Geocoder(this, Locale.getDefault())

        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                // In this sample, we get just a single address.
                1)
        } catch (ioException: IOException) {
            // Fall back to Geocoding web API
            Timber.e(ioException, "Service not available, falling back to web api...")
            fallBackToGeocodingWebApi(location)
            return
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Timber.e(illegalArgumentException, "Invalid lat/long")
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            Timber.e("Couldn't find address")
            deliverResultToReceiver(Constants.GEOCODING_FAILED_RESULT,"Unknown Address")
        } else {
            val address = addresses[0]
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            val addressFragments = with(address) {
                (0..maxAddressLineIndex).map { getAddressLine(it) }
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                addressFragments.joinToString(separator = "\n"))
        }
    }

    private fun fallBackToGeocodingWebApi(location: LatLng) {
        val urlRequestString = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${location.latitude},${location.longitude}&key=" + resources.getString(
            R.string.google_maps_key)

        urlRequestString.httpGet()
            .responseJson { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Timber.e(ex, "GeocodingWebAPI also failed, check internet...")
                        deliverResultToReceiver(Constants.NO_INTERNET_RESULT, "")
                    }
                    is Result.Success -> {
                        val jsonResponse = result.get().obj()
                        if (jsonResponse.getString("status") == "OK") {
                            val firstAddressMatch = jsonResponse.getJSONArray("results").getJSONObject(0)
                            val address = firstAddressMatch.getString("formatted_address")
                            deliverResultToReceiver(Constants.SUCCESS_RESULT, address)
                        } else {
                            Timber.e("Response status not OK: Response status = ${jsonResponse.getString("status")}")
                            deliverResultToReceiver(Constants.GEOCODING_FAILED_RESULT,"Unknown Street")
                        }
                    }
                }
            }.join()
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply { putString(Constants.RESULT_DATA_KEY, message) }
        receiver?.send(resultCode, bundle)
    }
}