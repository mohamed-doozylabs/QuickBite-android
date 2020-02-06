package com.griffsoft.tsadadelivery.ui.delivery


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.asPriceString
import com.griffsoft.tsadadelivery.extras.OnItemClickListener
import com.griffsoft.tsadadelivery.objects.MenuItem
import com.griffsoft.tsadadelivery.objects.Restaurant
import com.squareup.picasso.Picasso

class MenuCategoryFragment : TDFragment(), OnItemClickListener {

    private lateinit var category: String
    private lateinit var restaurant: Restaurant
    private lateinit var menuItemsInCategory: ArrayList<MenuItem>

    private lateinit var menuCategoryAdapter: MenuCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        category = arguments!!.getString("category")!!
        restaurant = arguments!!.getParcelable("restaurant")!!
        menuItemsInCategory = restaurant.getItemsInCategory(category)

        menuCategoryAdapter = MenuCategoryAdapter(context!!, menuItemsInCategory, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_menu_category, container, false)
        setupBackButton(root)

        root.findViewById<TextView>(R.id.category).text = category

        root.findViewById<RecyclerView>(R.id.menuItemsRecyclerView).apply {
            addItemDecoration(DividerItemDecoration(context!!, LinearLayoutManager.VERTICAL))
            layoutManager = LinearLayoutManager(context!!)
            adapter = menuCategoryAdapter
        }

        return root
    }

    override fun itemWasSelected(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        val menuItemIntent = Intent(activity!!, MenuItemActivity::class.java)
        menuItemIntent.putExtra("menuItem", menuItemsInCategory[position])
        menuItemIntent.putExtra("restaurant", restaurant)
        startActivity(menuItemIntent)
    }

    inner class MenuCategoryAdapter(val context: Context,
                                    private val menuItemsInCategory: ArrayList<MenuItem>,
                                    val itemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<MenuCategoryAdapter.MenuCategoryItemViewHolder>() {

        override fun getItemCount(): Int {
            return menuItemsInCategory.size
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MenuCategoryItemViewHolder {
            return MenuCategoryItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.menu_category_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: MenuCategoryItemViewHolder, position: Int) {
            val menuItem = menuItemsInCategory[position]

            if (menuItem.itemImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(menuItem.itemImageUrl)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.placeholder)!!)
                    .into(holder.itemImage)
            } else {
                holder.itemImage.visibility = View.GONE
            }

            holder.itemName.text = menuItem.itemName

            if (menuItem.description.isNotEmpty()) {
                holder.itemDescription.text = menuItem.description
            } else {
                holder.itemDescription.visibility = View.GONE
            }

            holder.itemPrice.text = menuItem.price.asPriceString
        }


        inner class MenuCategoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemImage: ImageView = view.findViewById(R.id.itemImage)
            val itemName: TextView = view.findViewById(R.id.itemName)
            val itemDescription: TextView = view.findViewById(R.id.itemDescription)
            val itemPrice: TextView = view.findViewById(R.id.itemPrice)

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
