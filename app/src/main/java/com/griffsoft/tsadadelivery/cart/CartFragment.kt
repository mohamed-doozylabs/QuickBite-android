package com.griffsoft.tsadadelivery.cart


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.griffsoft.tsadadelivery.*
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.concurrent.schedule

enum class PaymentMethod {
    Cash, GCash, Card
}

class CartFragment : TDFragment(), View.OnClickListener {

    private lateinit var removeAllButton: Button
    private lateinit var continueButton: Button

    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: ArrayList<MenuItem>

    private lateinit var paymentMethodBottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cartItems = Cart.getItems(context!!)

        cartAdapter = CartAdapter(context!!, cartItems)

        paymentMethodBottomSheetDialog = BottomSheetDialog(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_cart, container, false)
        setupBackButton(root)

        // Setup payment method bottom sheet
        val bottomSheetView = inflater.inflate(R.layout.payment_method_bottom_sheet, container, false)
        bottomSheetView.findViewById<Button>(R.id.cashButton).setOnClickListener(this)
        bottomSheetView.findViewById<Button>(R.id.gcashButton).setOnClickListener(this)
        bottomSheetView.findViewById<Button>(R.id.creditCardButton).setOnClickListener(this)
        paymentMethodBottomSheetDialog.setContentView(bottomSheetView)


        removeAllButton = root.findViewById(R.id.removeAllButton)
        removeAllButton.setOnClickListener(this)

        continueButton = root.findViewById(R.id.continueButton)
        continueButton.setOnClickListener(this)

        val itemsCount = Cart.getItems(context!!).size
        root.findViewById<TextView>(R.id.restaurantAndTotalQuantity).text = "${Cart.getRestaurant(context!!)!!.name} · ${itemsCount} item${if (itemsCount > 1) "s" else ""}"

        val cartTotal = Cart.getTotalPrice(context!!)
        root.findViewById<TextView>(R.id.cartTotalValue).text = cartTotal.asPriceString
        root.findViewById<TextView>(R.id.deliveryFeeValue).text = "₱50"

        root.findViewById<TextView>(R.id.totalValue).text = (cartTotal + 50).asPriceString

        root.findViewById<RecyclerView>(R.id.cartItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = cartAdapter
        }

        return root
    }

    private fun removeItem(index: Int) {
        cartItems.removeAt(index)
        cartAdapter.notifyDataSetChanged()

        Cart.removeItemAt(context!!, index)

        if (cartItems.isEmpty()) {
            dismissAfterEmpty()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.removeAllButton -> removeAll()
            R.id.continueButton -> continueButtonTapped()

            R.id.cashButton -> setPaymentMethodAndContinue(PaymentMethod.Cash)
            R.id.gcashButton -> setPaymentMethodAndContinue(PaymentMethod.GCash)
            R.id.creditCardButton -> setPaymentMethodAndContinue(PaymentMethod.Card)
        }
    }

    private fun removeAll() {
        cartItems.clear()
        cartAdapter.notifyDataSetChanged()
        Cart.empty(context!!)
        dismissAfterEmpty()
    }

    private fun dismissAfterEmpty() {
        removeAllButton.isEnabled = false
        continueButton.isEnabled = false
        Timer("cartDismissTimer", false).schedule(1000) {
            activity!!.finish()
        }
    }

    private fun continueButtonTapped() {
        paymentMethodBottomSheetDialog.show()
    }

    private fun setPaymentMethodAndContinue(paymentMethod: PaymentMethod) {
        paymentMethodBottomSheetDialog.dismiss()
        Cart.paymentMethod = paymentMethod

        val currentUser = UserUtil.getCurrentUser(context!!)!!

        if (currentUser.name.isEmpty() || currentUser.phone.isEmpty()) {
            performSegue(R.id.action_cartFragment_to_finalizeOrderFragment, bundleOf("finalizeOrderMode" to true))
        } else {
            performSegue(R.id.action_cartFragment_to_reviewAndPlaceOrderFragment)
        }
    }


    inner class CartAdapter(val context: Context,
                            private val cartItems: ArrayList<MenuItem>) :
        RecyclerView.Adapter<CartAdapter.CartItemViewHolder>() {

        override fun getItemCount(): Int {
            return cartItems.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
            return CartItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cart_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
            val item = cartItems[position]

            if (item.itemImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(item.itemImageUrl)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.placeholder)!!)
                    .into(holder.itemImage)
            } else {
                holder.itemImage.visibility = View.GONE
            }

            holder.quantity.text = "${item.selectedQuantity}x"
            holder.itemName.text = item.itemName
            holder.price.text = item.finalPrice.asPriceString

            if (item.selectedOptions != "None") {
                holder.itemOptions.text = item.selectedOptions
            } else {
                holder.itemOptions.visibility = View.GONE
            }

            if (item.specialInstructions != "") {
                holder.specialInstructions.text = "Instructions: ${item.specialInstructions}"
            } else {
                holder.specialInstructions.visibility = View.GONE
            }
        }

        inner class CartItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemImage: ImageView = view.findViewById(R.id.itemImage)
            val quantity: TextView = view.findViewById(R.id.quantity)
            val itemName: TextView = view.findViewById(R.id.itemName)
            val price: TextView = view.findViewById(R.id.itemPrice)
            val itemOptions: TextView = view.findViewById(R.id.itemOptions)
            val specialInstructions: TextView = view.findViewById(R.id.specialInstructions)

            private val menuButton: ImageView = view.findViewById(R.id.menuButton)

            init {
                menuButton.setOnClickListener {
                    val popup = PopupMenu(menuButton.context, menuButton)

                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.remove -> removeItem(adapterPosition)
                        }

                        true
                    }

                    popup.inflate(R.menu.cart_menu)
                    popup.show()
                }
            }
        }
    }
}

//class BottomSheetFragment: BottomSheetDialogFragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.payment_method_bottom_sheet, container, false)
//    }
//}
