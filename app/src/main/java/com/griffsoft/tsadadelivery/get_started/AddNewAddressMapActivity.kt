package com.griffsoft.tsadadelivery.get_started

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.GeoPoint
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.FetchAddressIntentService
import com.griffsoft.tsadadelivery.objects.Address
import com.griffsoft.tsadadelivery.px
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.dialog_rename_address.view.*

class AddNewAddressMapActivity : TDActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {
    companion object {
        const val PLACE_ID = "unique_place"
    }

    private lateinit var gMap: GoogleMap
    private lateinit var selectedPlace: Place
    private lateinit var resultReceiver: AddressResultReceiver
    private var addressOutput: String? = null
    private var mapWasMoved: Boolean = false

    private var userStreetNickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        originalConstraintSet.clone(rootConstraintLayout)

        resultReceiver = AddressResultReceiver(Handler())

        selectedPlace = intent.getParcelableExtra(PLACE_ID)
        streetTextView.text = selectedPlace.name

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun renameTapped(v: View) {
        val renameDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rename_address, null)

        AlertDialog.Builder(this)
            .setView(renameDialogView)
            .setTitle("Rename Address")
            .setNegativeButton("cancel", null)
            .setPositiveButton("rename") { _, _ ->
                val newName = renameDialogView.renameEditText.text.toString()

                if (newName.isNotEmpty()) {
                    userStreetNickname = newName
                    streetTextView.text = newName
                }
            }.show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlace.latLng, 17.5f))

        gMap.setOnCameraIdleListener(this)
        gMap.setOnCameraMoveListener(this)

        showPleaseMoveHint(true)
    }

    override fun onCameraIdle() {
        if (!mapWasMoved) { return }

        geocoderLoadingCover.visibility = View.VISIBLE
        saveAddressButton.isEnabled = false

        startReverseGeocodingService(gMap.cameraPosition.target)
    }

    override fun onCameraMove() {
        mapWasMoved = true
        userStreetNickname = null
        showPleaseMoveHint(false)
    }

    private fun showPleaseMoveHint(show: Boolean) {
        val altConstraintSet = ConstraintSet()
        altConstraintSet.clone(rootConstraintLayout)
        altConstraintSet.connect(R.id.pinHint, ConstraintSet.TOP, R.id.headerBackgroundView, ConstraintSet.BOTTOM, 8.px)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0f)
        transition.startDelay = 300
        transition.duration = 400

        TransitionManager.beginDelayedTransition(rootConstraintLayout, transition)
        val constraintSet = if (show) altConstraintSet else originalConstraintSet
        constraintSet.applyTo(rootConstraintLayout)
    }

    private fun startReverseGeocodingService(lastLocation: LatLng) {
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver)
            putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation)
        }
        startService(intent)
    }

    private fun displayAddressOutput() {
        geocoderLoadingCover.visibility = View.GONE
        saveAddressButton.isEnabled = true
        streetTextView.text = addressOutput!!.substringBefore(", Cag")
    }


    fun saveAddress() {
        val makeDefault = UserUtil.getCurrentUser(this)!!.addresses.isEmpty()

        val street = if (mapWasMoved)
            addressOutput!!.substringBefore(", Cag")
        else
            selectedPlace.address!!.substringBefore(", Cag")

        val geoPoint = if (mapWasMoved)
            GeoPoint(gMap.cameraPosition.target.latitude, gMap.cameraPosition.target.longitude)
        else
            GeoPoint(selectedPlace.latLng!!.latitude, selectedPlace.latLng!!.longitude)

        val newAddress = Address(userNickname = userStreetNickname ?: "",
            floorDoorUnitNo = floorDoorUnitEditText.text.toString(),
            street = street,
            buildingLandmark = buildingLandmarkEditText.text.toString(),
            instructions = instructionsEditText.text.toString(),
            geoPoint = geoPoint,
            isSelected = true,
            isDefault = makeDefault)

        UserUtil.addAddress(this, newAddress)


    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY) ?: ""
            displayAddressOutput()
        }
    }
}