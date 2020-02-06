package com.griffsoft.tsadadelivery.get_started

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity
import timber.log.Timber
import java.util.*

const val RC_FINISH = 999

class AddNewAddressSearchActivity : TDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_address_search)
        setResult(RC_DID_RETURN_TO_LOGIN)

        // Initialize the SDK
        Places.initialize(applicationContext, "AIzaSyDA9qrmg1UNFPnlAZWC1Xlis5TdkNIzavM")

        // Create a new Places client instance
//        val placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        // NOTE: RectangularBounds takes a southwest corner first, then a northeast corner.
        // The iOS code's coordinates are crisscrossed (lat 8.4 goes with long 124.65, and vice versa)
        autocompleteFragment!!.setLocationRestriction(
            RectangularBounds.newInstance(
                LatLng(8.422772, 124.615774),
                LatLng(8.504086, 124.657019)
            )
        )

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setHint("Add a delivery address...")

        // Change fragment icon to Address icon
        val autocompleteLayout = autocompleteFragment.view!! as LinearLayout
        val searchIcon = autocompleteLayout[0] as ImageView
        val addressIcon = ContextCompat.getDrawable(this,
            R.drawable.ic_map_marker
        )
        addressIcon!!.setTint(ContextCompat.getColor(this, R.color.colorAccent))
        searchIcon.setImageDrawable(addressIcon)

        // Automatically set focus when activity is opened
        autocompleteLayout.findViewById<AppCompatImageButton>(R.id.places_autocomplete_search_button).performClick()

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Timber.d("Place: " + place.name + ", " + place.id)
                segueToAddNewAddressMapActivity(place)
            }

            override fun onError(status: Status) {
                Timber.e("An error occurred: $status")
            }
        })
    }

    private fun segueToAddNewAddressMapActivity(place: Place) {
        val addNewAddressIntent = Intent(this, AddNewAddressMapActivity::class.java)
        addNewAddressIntent.putExtra(AddNewAddressMapActivity.PLACE_ID, place)
        startActivityForResult(addNewAddressIntent, RC_FINISH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_FINISH) {
            finish()
        }
    }
}