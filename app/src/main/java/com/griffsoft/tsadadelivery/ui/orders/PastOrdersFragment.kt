package com.griffsoft.tsadadelivery.ui.orders


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.asPriceString
import com.griffsoft.tsadadelivery.extras.OrderUtil
import com.griffsoft.tsadadelivery.objects.Order
import com.squareup.picasso.Picasso

@SuppressLint("SetTextI18n")
class PastOrdersFragment : TDFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val navigatedToFromCurrentOrder = arguments?.getBoolean("navigatedToFromCurrentOrder") ?: false

        if (!navigatedToFromCurrentOrder && UserUtil.getCurrentUser(context!!)!!.currentOrder != null) {
            val options = NavOptions.Builder().setPopUpTo(R.id.pastOrdersFragment, true).build()
            findNavController().navigate(R.id.action_pastOrdersFragment_to_currentOrderFragment, null, options)
            return null
        }

        val root = inflater.inflate(R.layout.fragment_past_orders, container, false)

        if (navigatedToFromCurrentOrder) {
            root.findViewById<ImageView>(R.id.backButton).visibility = View.VISIBLE
            root.findViewById<TextView>(R.id.pastOrders).textSize = 28f
            setupBackButton(root)
        }

        val pastOrders = UserUtil.getCurrentUser(context!!)!!.pastOrders

        if (pastOrders.isNotEmpty()) {
            pastOrders.sortByDescending { it.datePlaced }
            root.findViewById<RecyclerView>(R.id.pastOrdersRecyclerView).apply {
                visibility = View.VISIBLE
                addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))
                layoutManager = LinearLayoutManager(context!!)
                adapter = PastOrdersAdapter(context!!, pastOrders)
            }
        }

        return root
    }

    inner class PastOrdersAdapter(val context: Context,
                                  private val pastOrders: MutableList<Order>) :
        RecyclerView.Adapter<PastOrdersAdapter.PastOrderViewHolder>() {

        override fun getItemCount(): Int {
            return pastOrders.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastOrderViewHolder {
            return PastOrderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.past_order_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: PastOrderViewHolder, position: Int) {
            val pastOrder = pastOrders[position]

            Picasso.get()
                .load(pastOrder.restaurantImageUrl)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.placeholder)!!)
                .into(holder.restaurantImage)
            holder.restaurantName.text = pastOrder.restaurantName
            holder.datePlaced.text = OrderUtil.getDate(pastOrder.datePlaced)

            pastOrder.items.forEach {
                val quantityTextView = TextView(context).apply {
                    text = "${it.selectedQuantity}x"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        1f)
                    gravity = Gravity.CENTER
                }
                holder.quantityLinearLayout.addView(quantityTextView)

                val itemNameTextView = TextView(context).apply {
                    text = it.itemName
                }
                holder.itemLinearLayout.addView(itemNameTextView)
            }

            holder.totalPice.text = pastOrder.total.asPriceString
        }

        inner class PastOrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val restaurantImage: ImageView = view.findViewById(R.id.restaurantImage)
            val restaurantName: TextView = view.findViewById(R.id.restaurantName)
            val datePlaced: TextView = view.findViewById(R.id.datePlaced)
            val quantityLinearLayout: LinearLayout = view.findViewById(R.id.quantityLinearLayout)
            val itemLinearLayout: LinearLayout = view.findViewById(R.id.itemLinearLayout)
            val totalPice: TextView = view.findViewById(R.id.totalPrice)
        }
    }
}
