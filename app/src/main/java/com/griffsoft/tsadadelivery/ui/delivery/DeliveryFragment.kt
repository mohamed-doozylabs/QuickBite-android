package com.griffsoft.tsadadelivery.ui.delivery

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.DistanceTime
import com.griffsoft.tsadadelivery.extras.DistanceTimeUtil
import com.griffsoft.tsadadelivery.extras.OnItemClickListener
import com.griffsoft.tsadadelivery.objects.Address
import com.griffsoft.tsadadelivery.objects.Restaurant
import com.griffsoft.tsadadelivery.ui.account.FragmentContainerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.all_restaurants_list_item.view.*
import kotlinx.android.synthetic.main.fragment_delivery.*
import kotlinx.android.synthetic.main.highlighted_restaurant_list_item.view.*
import kotlinx.android.synthetic.main.highlighted_restaurant_list_item.view.restaurantImage
import kotlinx.android.synthetic.main.highlighted_restaurant_list_item.view.restaurantName
import kotlin.math.round

class DeliveryFragment : Fragment(), View.OnClickListener, OnItemClickListener, ViewTreeObserver.OnScrollChangedListener {

//    private lateinit var deliveryViewModel: DeliveryViewModel
    private lateinit var highlightedRestaurantsAdapter: HighlightedRestaurantsAdapter
    private lateinit var highlightedRestaurantsRecyclerView: RecyclerView
    private lateinit var allRestaurantsAdapter: AllRestaurantsAdapter
    private lateinit var allRestaurantsRecyclerView: RecyclerView

    private lateinit var addressLabel: TextView
    private lateinit var headerLayout: ConstraintLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var loadingView: ConstraintLayout

    private var allRestaurants: ArrayList<Restaurant> = arrayListOf()
    private var highlightedRestaurants: ArrayList<Restaurant> = arrayListOf()

    private lateinit var selectedAddress: Address

    private var sortByTime: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        deliveryViewModel = ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_delivery, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        deliveryViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        selectedAddress = UserUtil.getCurrentUser(context!!)!!.selectedAddress

        headerLayout = root.findViewById(R.id.deliveryHeaderLayout)
        headerLayout.setOnClickListener(this)
        scrollView = root.findViewById(R.id.scrollView)
        scrollView.viewTreeObserver.addOnScrollChangedListener(this)


        loadingView = root.findViewById(R.id.loadingViewLayout)
        loadingView.visibility = View.VISIBLE

        addressLabel = root.findViewById(R.id.addressLabel)
        addressLabel.text = selectedAddress.displayName

        allRestaurantsRecyclerView = root.findViewById(R.id.allRestaurantsRecyclerView)
        allRestaurantsRecyclerView.addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))
        allRestaurantsRecyclerView.layoutManager = LinearLayoutManager(context!!)
        allRestaurantsAdapter = AllRestaurantsAdapter(context!!, allRestaurants, this)
        allRestaurantsRecyclerView.adapter = allRestaurantsAdapter

        highlightedRestaurantsRecyclerView = root.findViewById(R.id.highlightedRestaurantsRecyclerView)
        highlightedRestaurantsRecyclerView.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        highlightedRestaurantsAdapter = HighlightedRestaurantsAdapter(context!!, highlightedRestaurants, this)
        highlightedRestaurantsRecyclerView.adapter = highlightedRestaurantsAdapter


        val db = FirebaseFirestore.getInstance().collection("restaurants")

        db.get().addOnSuccessListener {
            setupRestaurants(it)
        }


        return root
    }

    override fun onResume() {
        super.onResume()

        // Check if selected address has changed
        val address = UserUtil.getCurrentUser(context!!)!!.selectedAddress
        if (address.id != selectedAddress.id) {
            // Refresh screen
            selectedAddress = address
            loadingViewLayout.visibility = View.VISIBLE
            addressLabel.text = selectedAddress.displayName
            setupRestaurants()
        }
    }

    private fun sortRestaurantsByTime(distanceTimes: MutableMap<String, DistanceTime>?) {
        allRestaurants.forEach { restaurant ->
            val restaurantDt = distanceTimes?.get(restaurant.id)

            if (restaurantDt != null) {
                restaurant.distanceTime = restaurantDt
            } else {
                // Google matrix API failed to find distance time.
                // Calculate distance manually and create rough time estimate
                // based off that
                manuallyCalculateDistanceTime(restaurant)
            }
        }

        // In the event that the Google distance matrix API completely fails,
        // sort the restaurants by distance rather than time. Also set a variable
        // so that the tableView and collectionViews know to display distance instead of time.
        if (distanceTimes != null) {
            allRestaurants.sortBy { it.distanceTime!!.timeValue }
        } else {
            allRestaurants.sortBy { it.distanceTime!!.distanceValue }
            sortByTime = false
        }
    }

    private fun manuallyCalculateDistanceTime(restaurant: Restaurant) {
        val distances = FloatArray(2) // Not sure if this has to be 2
        Location.distanceBetween(restaurant.geoPoint.latitude,
                                 restaurant.geoPoint.longitude,
                                 selectedAddress.geoPoint.latitude,
                                 selectedAddress.geoPoint.longitude,
                                 distances)

        val distance = round((distances[0] / 1000) * 10) / 10

        restaurant.distanceTime = DistanceTime("MANUAL",
                                               "${distance}km",
                                               (distance * 1000).toInt(),
                                               "45 mins",
                                               45)
    }

    private fun setupRestaurants(documents: QuerySnapshot? = null) {
        documents?.let { snapshot ->
            allRestaurants.addAll( snapshot.map { it.toObject(Restaurant::class.java) })
        }

        DistanceTimeUtil.getDistanceTimes(context!!, allRestaurants, selectedAddress) { result, error ->
            sortRestaurantsByTime(result)
            populateHighlightedCategories()
            allRestaurantsAdapter.notifyDataSetChanged()
            loadingView.visibility = View.GONE
        }
    }

    private fun populateHighlightedCategories() {
        highlightedRestaurants.addAll(allRestaurants.filter { it.topPick })
        highlightedRestaurantsAdapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.deliveryHeaderLayout -> switchAddress()
        }
    }

    override fun onScrollChanged() {
        if (scrollView.scrollY > 10) {
            if (headerLayout.translationZ == 0f) {
                headerLayout.translationZ = 20f
            }
        } else {
            headerLayout.translationZ = 0f
        }
    }

    override fun itemWasSelected(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun switchAddress() {
        startActivity(Intent(activity, FragmentContainerActivity::class.java))
    }

    inner class HighlightedRestaurantsAdapter(val context: Context,
                                              private val restaurants: ArrayList<Restaurant>,
                                              val itemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<HighlightedRestaurantsAdapter.HighlightedRestaurantItemViewHolder>() {

        override fun getItemCount(): Int {
            return restaurants.size
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HighlightedRestaurantItemViewHolder {
            return HighlightedRestaurantItemViewHolder(LayoutInflater.from(context).inflate(R.layout.highlighted_restaurant_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: HighlightedRestaurantItemViewHolder, position: Int) {
            val restaurant = restaurants[position]

            Picasso.get().load(restaurant.imageUrl).into(holder.restaurantImage)
            holder.restaurantName.text = restaurant.name
            holder.deliveryTimeAndFee.text = if (sortByTime) "${restaurant.distanceTime!!.time} · Free delivery" else "${restaurant.distanceTime!!.distance} · Free delivery"
        }

        inner class HighlightedRestaurantItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val restaurantImage: ImageView = view.restaurantImage
            val restaurantName: TextView = view.restaurantName
            val deliveryTimeAndFee: TextView = view.deliveryTimeAndFee

            init {
                view.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemClickListener.itemWasSelected(adapterPosition)
                    }
                }
            }
        }
    }

    inner class AllRestaurantsAdapter(val context: Context,
                                      private val restaurants: List<Restaurant>,
                                      val itemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<AllRestaurantsAdapter.RestaurantViewHolder>() {

        override fun getItemCount(): Int {
            return restaurants.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
            return RestaurantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.all_restaurants_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
            val restaurant = restaurants[position]

            Picasso.get().load(restaurant.imageUrl).into(holder.restaurantImage)
            holder.restaurantName.text = restaurant.name
            holder.restaurantCategories.text = restaurant.categories
            holder.deliveryEstimate.text = if (sortByTime) restaurant.distanceTime!!.time else restaurant.distanceTime!!.distance
            holder.rating.text = restaurant.rating.toString()
            holder.deliveryFee.text = "₱50 delivery"
        }

        inner class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            internal var restaurantImage: ImageView = view.restaurantImage
            internal var restaurantName: TextView = view.restaurantName
            internal var deliveryEstimate: TextView = view.deliveryEstimate
            internal var restaurantCategories: TextView = view.restaurantCategories
            internal var rating: TextView = view.rating
            internal var deliveryFee: TextView = view.deliveryFee

            init {
                view.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemClickListener.itemWasSelected(adapterPosition)
                    }
                }
            }
        }
    }
}