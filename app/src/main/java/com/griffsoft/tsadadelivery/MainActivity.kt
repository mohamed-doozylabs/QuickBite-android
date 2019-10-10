package com.griffsoft.tsadadelivery

import Order
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.order_list_item.view.*

class MainActivity : AppCompatActivity(), OrderRecyclerViewDelegate {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)

        recyclerView = findViewById<RecyclerView>(R.id.orders_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager
        }

        val query = FirebaseFirestore.getInstance().collection("orders").orderBy("lastUpdated")

        val options = FirestoreRecyclerOptions.Builder<Order>()
            .setQuery(query, Order::class.java)
            .setLifecycleOwner(this)
            .build()

        val orderAdapter = OrderAdapter(options, this)
        orderAdapter.delegate = this
        recyclerView.adapter = orderAdapter
    }

    override fun orderWasSelected(documentSnapshot: DocumentSnapshot, position: Int) {
        val orderIntent = Intent(this, OrderActivity::class.java)
        orderIntent.putExtra(OrderActivity.ORDER_ID, documentSnapshot.id)
        startActivity(orderIntent)
    }
}


class OrderAdapter(options: FirestoreRecyclerOptions<Order>, context: Context): FirestoreRecyclerAdapter<Order, OrderAdapter.OrderHolder>(options) {
    var delegate: OrderRecyclerViewDelegate? = null
    private val r = context.resources

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.order_list_item,parent,false)
        return OrderHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderHolder, position: Int, model: Order) {

        holder.orderId.text = r.getString(R.string.order, model.restaurantName)
        holder.placedDate.text = r.getString(R.string.placed, OrderUtil.formatDate(model.datePlaced))
        holder.lastUpdatedDate.text = r.getString(R.string.last_updated, OrderUtil.formatDate(model.lastUpdated))
        holder.orderStatus.text = r.getString(R.string.order_status, OrderUtil.interpretOrderStage(model.currentStage))
    }

    inner class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var orderId: TextView = itemView.orderIdText
        internal var placedDate: TextView = itemView.datePlacedText
        internal var lastUpdatedDate: TextView = itemView.lastUpdatedText
        internal var orderStatus: TextView = itemView.orderInProgressText

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && delegate != null) {
                    delegate!!.orderWasSelected(snapshots.getSnapshot(position), position)
                }
            }
        }
    }
}

interface OrderRecyclerViewDelegate {
    fun orderWasSelected(documentSnapshot: DocumentSnapshot, position: Int)
}





