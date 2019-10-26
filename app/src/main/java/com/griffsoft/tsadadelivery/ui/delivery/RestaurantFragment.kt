package com.griffsoft.tsadadelivery.ui.delivery


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.griffsoft.tsadadelivery.MenuItem
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.asPriceString
import com.griffsoft.tsadadelivery.extras.OnItemClickListener
import com.griffsoft.tsadadelivery.objects.Restaurant
import com.squareup.picasso.Picasso

class RestaurantFragment : TDFragment(), OnItemClickListener {

    private lateinit var restaurant: Restaurant

    private lateinit var featuredItemsAdapter: FeaturedItemsAdapter
    private lateinit var fullMenuAdapter: FullMenuAdapter

    private var featuredItems = arrayListOf<MenuItem>()
    private var menuCategories = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restaurant = arguments!!.getParcelable("restaurant")!!

        featuredItems = restaurant.getFeaturedItems()
        menuCategories = restaurant.getMenuCategories()

        featuredItemsAdapter = FeaturedItemsAdapter(context!!, featuredItems, this)
        fullMenuAdapter = FullMenuAdapter(context!!, menuCategories, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_restaurant, container, false)
        setupBackButton(root)

        if (featuredItems.isEmpty()) {
            root.findViewById<ConstraintLayout>(R.id.featuredItemsLayout).visibility = View.GONE
        }

        root.findViewById<TextView>(R.id.restaurantName).text = restaurant.name
        root.findViewById<TextView>(R.id.restaurantCategories).text = restaurant.categories
        root.findViewById<TextView>(R.id.restaurantAddress).text = "${restaurant.distanceTime!!.distance} · ${restaurant.address.substringBefore(", Cag")}"
        root.findViewById<TextView>(R.id.deliveryFee).text = "₱50"
        root.findViewById<TextView>(R.id.timeEstimate).text = restaurant.distanceTime!!.time.substringBefore(" ")

        root.findViewById<RecyclerView>(R.id.featuredItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredItemsAdapter
            setHasFixedSize(true)
        }

        root.findViewById<RecyclerView>(R.id.fullMenuRecyclerView).apply {
            addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))
            layoutManager = LinearLayoutManager(context!!)
            adapter = fullMenuAdapter
            setHasFixedSize(true)
        }

        return root
    }

    override fun itemWasSelected(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder is FeaturedItemsAdapter.FeaturedItemsViewHolder) {
            val menuItemIntent = Intent(activity!!, MenuItemActivity::class.java)
            menuItemIntent.putExtra("menuItem", featuredItems[position])
            startActivity(menuItemIntent)
        }
    }

    inner class FeaturedItemsAdapter(val context: Context,
                                     private val featuredItems: ArrayList<MenuItem>,
                                     val itemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<FeaturedItemsAdapter.FeaturedItemsViewHolder>() {

        override fun getItemCount(): Int {
            return featuredItems.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeaturedItemsViewHolder {
            return FeaturedItemsViewHolder(LayoutInflater.from(context).inflate(R.layout.featured_item_list_item, parent, false))

        }

        override fun onBindViewHolder(holder: FeaturedItemsViewHolder, position: Int) {
            val featuredItem = featuredItems[position]

            Picasso.get().load(featuredItem.itemImageUrl).into(holder.featuredItemImage)
            holder.featuredItemName.text = featuredItem.itemName
            holder.itemPrice.text = featuredItem.price.asPriceString

        }

        inner class FeaturedItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val featuredItemImage: ImageView = view.findViewById(R.id.featuredItemImage)
            val featuredItemName: TextView = view.findViewById(R.id.featuredItemName)
            val itemPrice: TextView = view.findViewById(R.id.itemPrice)

            init {
                view.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemClickListener.itemWasSelected(adapterPosition, this)
                    }
                }
            }
        }
    }

    inner class FullMenuAdapter(val context: Context,
                                private val menuCategories: ArrayList<String>,
                                val itemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<FullMenuAdapter.FullMenuCategoryViewHolder>() {

        override fun getItemCount(): Int {
            return menuCategories.size
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FullMenuCategoryViewHolder {
            return FullMenuCategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.full_menu_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: FullMenuCategoryViewHolder, position: Int) {
            val menuCategory = menuCategories[position]

            holder.menuCategory.text = menuCategory
            holder.categoryItemCount.text = restaurant.getItemsInCategory(menuCategory).size.toString()
        }

        inner class FullMenuCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val menuCategory: TextView = view.findViewById(R.id.menuCategory)
            val categoryItemCount: TextView = view.findViewById(R.id.categoryItemCount)

            init {
                view.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemClickListener.itemWasSelected(adapterPosition, this)
                    }
                }
            }
        }
    }
}
