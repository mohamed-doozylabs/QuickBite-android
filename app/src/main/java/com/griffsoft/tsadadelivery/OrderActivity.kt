@file:Suppress("UNUSED_PARAMETER")

package com.griffsoft.tsadadelivery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.objects.Order
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.order_item_list_item.view.*
import timber.log.Timber


class OrderActivity : AppCompatActivity(), OrderStatusDialogFragment.OrderStatusDialogDelegate {
    companion object {
        const val ORDER_ID = "order_id"
    }

    private var order: Order? = null
    private var orderRef: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        itemsList.layoutManager = LinearLayoutManager(this)

        val orderId = intent.getStringExtra(ORDER_ID)
        orderRef = FirebaseFirestore.getInstance().collection("orders").document(orderId)

        orderRef!!.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.w(e, "Listen failed.")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                order = snapshot.toObject(Order::class.java)
                populateOrderInfo()
                if (loadingViewLayout.visibility == View.VISIBLE) {
                    loadingViewLayout.visibility = View.GONE
                }
            } else {
                Timber.d("Current data: null")
            }
        }
    }

    private fun populateOrderInfo() {
        itemsList.adapter = OrderItemAdapter(order!!.items, this)

        orderTitle.text = resources.getString(R.string.order, order!!.restaurantName)
        orderStatus.text = resources.getString(R.string.order_status, OrderUtil.interpretOrderStage(order!!.currentStage))
        datePlaced.text = resources.getString(R.string.placed_and_date, OrderUtil.formatDate(order!!.datePlaced), OrderUtil.getDate(order!!.datePlaced))
        lastUpdated.text = resources.getString(R.string.last_updated_and_date, OrderUtil.formatDate(order!!.lastUpdated), OrderUtil.getDate(order!!.lastUpdated))

        restaurantAddress.text = resources.getString(R.string.restaurant_address, order!!.restaurantAddress)
        restuarantNumber.text = resources.getString(R.string.restaurant_contact_number, order!!.restaurantContactNumber)

        orderTotal.text = resources.getString(R.string.order_total, order!!.total)
        paymentMethod.text = resources.getString(R.string.payment_method, order!!.paymentMethod)

        customerName.text = resources.getString(R.string.customer_name, order!!.customerName)
        customerNumber.text = resources.getString(R.string.customer_contact_number, order!!.customerContactNumber)

        deliveryAddressFullString.text = order!!.deliveryAddress.fullString
        deliveryAddressInstructions.text = resources.getString(R.string.delivery_address_instructions, order!!.deliveryAddress.instructions)
    }

    fun openDeliveryAddressInGoogleMaps(view: View) {
        val lat = order!!.deliveryAddress.geoPoint.latitude
        val long = order!!.deliveryAddress.geoPoint.longitude
        val addressString = order!!.deliveryAddress.fullString

        val gmmIntentUri = Uri.parse("geo:$lat,$long?z=16.5&q=$lat,$long($addressString)")

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps")

        // Attempt to start an activity that can handle the Intent
        startActivity(mapIntent)
    }

    fun callRestaurant(v: View) {
        call(order!!.restaurantContactNumber)
    }

    fun callCustomer(v: View) {
        call(order!!.customerContactNumber)
    }

    private fun call(number: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse("tel:$number")
        startActivity(callIntent)
    }

    fun changeStatus(v: View) {
        val changeStatusFragment = OrderStatusDialogFragment.newInstance(order!!.currentStage)
        changeStatusFragment.show(supportFragmentManager, "ChangeStatus_tag")
    }

    override fun newOrderStatusSelected(newStage: Int) {
        orderRef!!.update("currentStage", newStage,
            "lastUpdated", Timestamp.now()).addOnSuccessListener {
            Toast.makeText(this, "Order Status updated", Toast.LENGTH_LONG).show()
        }
        sendSmsNotification()
    }

    private fun sendSmsNotification() {
        //TODO: Implement Sms Notifications
    }

    fun sendInfoToDriver(v: View) {
        //TODO: Finish this method
        var message = "NEW ORDER\n" +
                "RESTO: ${order!!.restaurantName}, ${order!!.restaurantAddress}\n" +
                "CUST: ${order!!.customerName}, ${order!!.customerContactNumber}\n" +
                "ITEMS: ${order!!.totalQuantity()} items total\n"

        order!!.items.forEach {
            message += "   ${it.selectedQuantity}x ${it.itemName}"
            if (it.selectedOptions != "None") {
                message += "(${it.selectedOptions})"
            }

            if (it.specialInstructions != "None") {
                message += "SPECIAL REQUEST: ${it.specialInstructions}"
            }
            message += "\n"
        }


        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.putExtra("sms_body", message)
        sendIntent.type = "vnd.android-dir/mms-sms"
        startActivity(sendIntent)
    }
}

class OrderItemAdapter(private val orderItems: List<MenuItem>,
                       private val context: Context) :
    RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    private val r = context.resources

    override fun getItemCount(): Int {
        return orderItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.order_item_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = orderItems[position]
        holder.quantityTv.text = r.getString(R.string.quantity, menuItem.selectedQuantity)
        holder.itemNameTv.text = menuItem.itemName
        holder.totalPriceTv.text = r.getString(R.string.total_price, menuItem.finalPrice)
        holder.orderOptionsTv.text = r.getString(R.string.selected_options, menuItem.selectedOptions)
        holder.instructionsTv.text = r.getString(R.string.special_instructions, menuItem.specialInstructions)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val quantityTv: TextView = view.quantity
        val itemNameTv: TextView = view.itemName
        val totalPriceTv: TextView = view.totalPrice
        val orderOptionsTv: TextView = view.orderOptions
        val instructionsTv: TextView = view.specialInstructions
    }
}
