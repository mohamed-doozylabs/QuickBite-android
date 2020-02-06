package com.griffsoft.tsadadelivery.ui.delivery


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.TDTabBarActivity
import com.griffsoft.tsadadelivery.extras.TDEditText
import com.griffsoft.tsadadelivery.objects.Restaurant
import java.util.*
import kotlin.collections.ArrayList


@SuppressLint("SetTextI18n")
class SearchFragment : TDFragment(), SearchResultSelectionListener {

    private lateinit var tdTabBarActivity: TDTabBarActivity

    private lateinit var searchEditText: TDEditText
    private lateinit var searchClearButton: ImageButton
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var popularSearchesLinearLayout: LinearLayout
    private lateinit var noResultsLayout: ConstraintLayout
    private var allRestaurants = arrayListOf<Restaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allRestaurants = arguments?.getParcelableArrayList("restaurants") ?: arrayListOf()
        searchAdapter = SearchAdapter(context!!, ArrayList(allRestaurants), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tdTabBarActivity = activity as TDTabBarActivity
        tdTabBarActivity.window.statusBarColor = ContextCompat.getColor(context!!, R.color.search_bar_background)
        tdTabBarActivity.setCartBannerVisibility(View.GONE)

        val root = inflater.inflate(R.layout.fragment_search, container, false)

        searchEditText = root.findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener {
            updateSearchResults(it.toString())
        }
        searchEditText.setFocus()
        searchClearButton = root.findViewById(R.id.searchClearButton)
        searchClearButton.setOnClickListener {
            searchEditText.text?.clear()
            searchEditText.setFocus()
        }

        searchResultsRecyclerView = root.findViewById<RecyclerView>(R.id.searchResultsRecyclerView).apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = searchAdapter
        }

        popularSearchesLinearLayout = root.findViewById(R.id.popularSearchesLinearLayout)

        noResultsLayout = root.findViewById(R.id.noResultsLayout)

        setupPopularSearches()

        setupBackButton(root, arrayListOf(searchEditText))
        return root
    }

    override fun onPause() {
        super.onPause()
        tdTabBarActivity.setCartBannerVisibility(View.VISIBLE)
        tdTabBarActivity.window.statusBarColor = Color.WHITE
    }

    override fun searchResultWasSelected(restaurant: Restaurant) {
        performSegue(R.id.action_searchFragment_to_restaurantFragment, bundleOf("restaurant" to restaurant))
    }

    private fun updateSearchResults(searchInput: String) {
        if (searchInput.isEmpty()) {
            noResultsLayout.visibility = View.GONE
            searchClearButton.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
            searchAdapter.repopulateSearchResults()
        } else {
            searchClearButton.visibility = View.VISIBLE
            searchAdapter.filter.filter(searchInput)
        }
    }

    private fun setupPopularSearches() {
        val searches = arrayListOf("inasal", "burgers", "chicken", "Kagayan Coffee Cartel")

        searches.forEach {search ->
            val button = LayoutInflater.from(context!!).inflate(R.layout.search_prompt_button, null) as Button
            popularSearchesLinearLayout.addView(
                button.apply {
                    text = "\"$search\""
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
                    setOnClickListener {
                        searchEditText.setText(search)
                        searchEditText.clearFocus()
                    }
                }
            )
        }
    }

    inner class SearchAdapter(val context: Context,
                              private val searchResultsArrayList: ArrayList<Restaurant>,
                              val itemClickListener: SearchResultSelectionListener) :
        RecyclerView.Adapter<SearchAdapter.SearchResultViewHolder>(), Filterable {
        private var allRestaurants = arrayListOf<Restaurant>().apply { addAll(searchResultsArrayList) }

        fun repopulateSearchResults() {
            searchResultsArrayList.clear()
            searchResultsArrayList.addAll(allRestaurants)
        }

        override fun getItemCount(): Int {
            return searchResultsArrayList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            return SearchResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_result_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
            val restaurant = searchResultsArrayList[position]

            holder.restaurant = restaurant

            holder.restaurantName.text = restaurant.name
            holder.restaurantCategories.text = restaurant.categories
            holder.timeEstimate.text = "${restaurant.distanceTime?.time}"
        }

        override fun getFilter(): Filter {
            return searchFilter
        }

        private val searchFilter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<Restaurant>()

                if (constraint != null && constraint.isNotEmpty()) {
                    val filterPattern = constraint.toString().toLowerCase(Locale.ENGLISH).trim { it <= ' ' }

                    searchResultsArrayList.forEach { restaurant ->
                        if (restaurant.name.toLowerCase(Locale.ENGLISH).contains(filterPattern)
                            || restaurant.categories.toLowerCase(Locale.ENGLISH).contains(filterPattern)) {
                            filteredList.add(restaurant)
                        }
                    }
                }

                return FilterResults().apply {
                    values = filteredList
                }
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {

                val searchResults = results.values as List<Restaurant>
                if (searchResults.isEmpty()) {
                    noResultsLayout.visibility = View.VISIBLE
                    searchResultsRecyclerView.visibility = View.GONE
                    repopulateSearchResults()
                } else {
                    noResultsLayout.visibility = View.GONE
                    searchResultsRecyclerView.visibility = View.VISIBLE
                    searchResultsArrayList.clear()
                    searchResultsArrayList.addAll(results.values as List<Restaurant>)
                    notifyDataSetChanged()
                }


            }
        }

        inner class SearchResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var restaurantName: TextView = view.findViewById(R.id.restaurantName)
            var restaurantCategories: TextView = view.findViewById(R.id.restaurantCategories)
            var timeEstimate: TextView = view.findViewById(R.id.timeEstimate)

            var restaurant: Restaurant? = null

            init {
                view.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        itemClickListener.searchResultWasSelected(restaurant!!)
                    }
                }
            }
        }
    }
}

interface SearchResultSelectionListener {
    fun searchResultWasSelected(restaurant: Restaurant)
}
