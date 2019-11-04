package com.griffsoft.tsadadelivery.cart


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.*
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.objects.Order
import com.griffsoft.tsadadelivery.ui.account.AddressesContainerActivity
import java.util.*

const val RC_UPDATE_CONTACT_INFO = 888
const val RC_UPDATE_ADDRESS = 777

@SuppressLint("SetTextI18n")
class ReviewAndPlaceOrderFragment : TDFragment(), View.OnClickListener {

    private lateinit var rootView: View
    private lateinit var placeOrderButton: Button
    private lateinit var paymentMethodBottomSheetDialog: BottomSheetDialog

    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_review_and_place_order, container, false)
        setupBackButton(rootView)

        currentUser = UserUtil.getCurrentUser(context!!)!!

        // Setup payment method bottom sheet
        paymentMethodBottomSheetDialog = BottomSheetDialog(context!!)
        val bottomSheetView = inflater.inflate(R.layout.payment_method_bottom_sheet, container, false)
        bottomSheetView.findViewById<Button>(R.id.cashButton).setOnClickListener(this)
        bottomSheetView.findViewById<Button>(R.id.gcashButton).setOnClickListener(this)
        bottomSheetView.findViewById<Button>(R.id.creditCardButton).setOnClickListener(this)
        paymentMethodBottomSheetDialog.setContentView(bottomSheetView)

        populateAddress()
        populateContactInfo(arguments?.getString("name"), arguments?.getString("phone"))
        populateOrderDetails()
        populateDeliveryEstimate()
        populatePaymentMethod()

        // Setup buttons
        rootView.findViewById<Button>(R.id.deliveryAddressButton).setOnClickListener(this)
        rootView.findViewById<Button>(R.id.contactInfoButton).setOnClickListener(this)
        rootView.findViewById<Button>(R.id.paymentMethodButton).setOnClickListener(this)
        placeOrderButton = rootView.findViewById(R.id.placeOrderButton)
        placeOrderButton.setOnClickListener(this)


        return rootView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.deliveryAddressButton -> changeAddress()
            R.id.contactInfoButton -> changeContactInfo()
            R.id.paymentMethodButton -> changePaymentMethod()
            R.id.placeOrderButton -> placeOrderTapped()

            R.id.cashButton -> updatePaymentMethod(PaymentMethod.Cash)
            R.id.gcashButton -> updatePaymentMethod(PaymentMethod.GCash)
            R.id.creditCardButton -> updatePaymentMethod(PaymentMethod.Card)
        }
    }

    private fun populateAddress() {
        val address = currentUser.selectedAddress

        val addressName: TextView = rootView.findViewById(R.id.addressName)
        val unitAndStreet: TextView = rootView.findViewById(R.id.unitAndStreet)
        val buildingLandmark: TextView = rootView.findViewById(R.id.buildingLandmark)
        val instructions: TextView = rootView.findViewById(R.id.instructions)

        addressName.text = address.displayName

        if (address.floorDoorUnitNo.isNotEmpty()) {
            if (address.userNickname.isNotEmpty()) {
                // First line is set to user nickname, so append street
                unitAndStreet.text = address.floorDoorUnitNo + ", " + address.street
            } else {
                unitAndStreet.text = address.floorDoorUnitNo
            }
        } else if (address.userNickname.isEmpty()) {
            // No unit or floor set, but there is a userNickname
            unitAndStreet.text = address.street
        } else {
            unitAndStreet.visibility = View.GONE
        }

        if (address.buildingLandmark.isNotEmpty()) {
            buildingLandmark.text = address.buildingLandmark
        } else {
            buildingLandmark.visibility = View.GONE
        }

        if (address.instructions.isNotBlank()) {
            instructions.text  = "Instructions: " + address.instructions
        } else {
            instructions.visibility = View.GONE
        }
    }

    private fun populateContactInfo(argName: String? = null, argPhone: String? = null) {
        var contactInfoString: String

        if (argName != null) {
            contactInfoString = argName
        } else {
            contactInfoString = currentUser.name
        }

        if (argPhone != null) {
            contactInfoString += "\n$argPhone"
        } else {
            contactInfoString += "\n${currentUser.phone}"
        }

        rootView.findViewById<TextView>(R.id.contactInfo).text = contactInfoString
    }

    private fun populateOrderDetails() {
        val cartQuantity = Cart.getTotalQuantity(context!!)
        val orderTotal = (Cart.getTotalPrice(context!!) + 50).asPriceString
        val s = if (cartQuantity > 1) "s" else ""
        rootView.findViewById<TextView>(R.id.orderDetails).text = "$cartQuantity item$s from ${Cart.getRestaurant(context!!)!!.name}\nTotal $orderTotal"
    }

    private fun populateDeliveryEstimate() {
        val cartRestaurant = Cart.getRestaurant(context!!)!!
        rootView.findViewById<TextView>(R.id.deliveryEstimate).text = cartRestaurant.distanceTime!!.time
    }

    private fun populatePaymentMethod() {
        rootView.findViewById<TextView>(R.id.paymentMethod).text = Cart.paymentMethod!!.name
    }

    private fun changeAddress() {
        startActivityForResult(Intent(activity, AddressesContainerActivity::class.java), RC_UPDATE_ADDRESS)
    }

    private fun changeContactInfo() {
        val contactInfo = rootView.findViewById<TextView>(R.id.contactInfo)
        val contactInfoIntent = Intent(activity, ContactInfoContainerActivity::class.java)
        contactInfoIntent.putExtra("name", contactInfo.text.toString().substringBefore("\n"))
        contactInfoIntent.putExtra("phone", contactInfo.text.toString().substringAfter("\n"))
        startActivityForResult(contactInfoIntent, RC_UPDATE_CONTACT_INFO)
    }

    private fun changePaymentMethod() {
        paymentMethodBottomSheetDialog.show()
    }

    private fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodBottomSheetDialog.dismiss()
        Cart.paymentMethod = paymentMethod
        populatePaymentMethod()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_UPDATE_CONTACT_INFO) {
            val resultName = data?.getStringExtra("name")
            val resultPhone = data?.getStringExtra("phone")
            populateContactInfo(resultName, resultPhone)
        } else if (requestCode == RC_UPDATE_ADDRESS) {
            currentUser = UserUtil.getCurrentUser(context!!)!!
            populateAddress()
        }
    }

    private fun placeOrderTapped() {
        if (!TDUtil.isNetworkReachable(context!!)) {
            AlertDialog.Builder(context!!)
                .setTitle("Error Placing Order")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton("Close", null)
                .show()
            return
        }

        AlertDialog.Builder(context!!)
            .setTitle("Confirm Order")
            .setMessage("You are about to place an order from ${Cart.getRestaurant(context!!)!!.name}")
            .setPositiveButton("Confirm") { _, _ ->
                rootView.isClickable = false
                saveUserInformation()
                placeOrder()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUserInformation() {
        val contactInfo = rootView.findViewById<TextView>(R.id.contactInfo)
        // Only save user information if the current saved name and phone are empty
        if (currentUser.name.isEmpty()) {
            UserUtil.setName(context!!, contactInfo.text.toString().substringBefore("\n"))
        }

        if (currentUser.phone.isEmpty()) {
            UserUtil.setPhone(context!!, contactInfo.text.toString().substringAfter("\n"))
        }
    }

    private fun placeOrder() {
        // Show activity indicator
        placeOrderButton.text = ""
        val orderProgressIndicator = rootView.findViewById<ProgressBar>(R.id.orderProgressIndicator)
        orderProgressIndicator.visibility = View.VISIBLE

        val contactInfo = rootView.findViewById<TextView>(R.id.contactInfo)
        val deliveryEstimate = rootView.findViewById<TextView>(R.id.deliveryEstimate)

        val restaurant = Cart.getRestaurant(context!!)!!
        val orderPlacedDate = Date()

        val order = Order(
            customerName = contactInfo.text.toString().substringBefore("\n"),
            customerContactNumber = contactInfo.text.toString().substringAfter("\n"),
            deliveryAddress = currentUser.selectedAddress,
            restaurantName = restaurant.name,
            restaurantAddress = restaurant.address,
            restaurantContactNumber = restaurant.contactNumber,
            restaurantImageUrl = restaurant.imageUrl,
            datePlaced = Timestamp(orderPlacedDate),
            lastUpdated = Timestamp(orderPlacedDate),
            items = Cart.getItems(context!!),
            total = Cart.getTotalPrice(context!!),
            deliveryTimeEstimate = deliveryEstimate.text.toString().substringBefore(" ").toInt(),
            paymentMethod = Cart.paymentMethod.toString(),
            currentStage = 0,
            notificationToken = TDUtil.getNotificationToken(context!!),
            userHasPushNotificationsEnabled = currentUser.pushNotificationsEnabled
        )

        UserUtil.addOrUpdateCurrentOrder(context!!, order)

        val ordersDb = FirebaseFirestore.getInstance().collection("orders")

        ordersDb.document(order.id).set(order.dictionary)
            .addOnFailureListener {
                rootView.isClickable = true
                AlertDialog.Builder(context!!)
                    .setTitle("Unable to Place Order")
                    .setMessage("Please try again. If problem persists, please contact us through the Account page.")
                    .setPositiveButton("Close", null)
                    .show()
                orderProgressIndicator.visibility = View.GONE
                placeOrderButton.text = "Place Order"
            }
            .addOnSuccessListener {
                orderProgressIndicator.visibility = View.GONE
                Cart.empty(context!!)
                TDUtil.showSuccessDialog(context!!, "Order Placed", showFor = 1750) {
                    activity!!.setResult(RC_REDIRECT_TO_ORDERS)
                    activity!!.finish()
                }
            }
    }

}
