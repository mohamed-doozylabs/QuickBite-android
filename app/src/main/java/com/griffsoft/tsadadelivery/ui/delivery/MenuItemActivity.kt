package com.griffsoft.tsadadelivery.ui.delivery

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.asPriceString
import com.griffsoft.tsadadelivery.cart.Cart
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.objects.MenuItem
import com.griffsoft.tsadadelivery.objects.MenuItemOption
import com.griffsoft.tsadadelivery.objects.Restaurant
import com.griffsoft.tsadadelivery.px
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_menu_item.*
import kotlinx.android.synthetic.main.content_menu_item.*

class MenuItemActivity : AppCompatActivity(), ViewTreeObserver.OnScrollChangedListener {

    private lateinit var menuItem: MenuItem
    private var usingImageLayout: Boolean = true
    private lateinit var restaurant: Restaurant

    private var singleSelectedOptions = mutableMapOf<Int, MenuItemOption>() // Used for single-selection option categories
    private var multiSelectedOptions = arrayListOf<MenuItemOption>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        menuItem = intent.getParcelableExtra("menuItem")
        restaurant = intent.getParcelableExtra("restaurant")

        usingImageLayout = menuItem.itemImageUrl.isNotEmpty()

        if (usingImageLayout) {
            setTheme(R.style.AppTheme_NoActionBar)
            setContentView(R.layout.activity_menu_item)
            toolbar.setNavigationIcon(R.drawable.close_small)
            setSupportActionBar(toolbar)
            window.statusBarColor = Color.TRANSPARENT

            Picasso.get()
                .load(menuItem.itemImageUrl)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.placeholder)!!)
                .into(header)
        } else {
            setContentView(R.layout.activity_menu_item_no_image)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.close_small)
            supportActionBar?.elevation = 0f
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scrollView.viewTreeObserver.addOnScrollChangedListener(this)

        menuItemName.text = menuItem.itemName

        if (menuItem.description.isNotEmpty()) {
            itemDescription.text = menuItem.description
        }

        price.text = menuItem.price.asPriceString

        setupMenuItemOptions()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupMenuItemOptions() {
        menuItem.itemOptionCategories.forEach {
            val optionLayout = LayoutInflater.from(this).inflate(R.layout.menu_item_option_layout, menuOptionsLayout, false)
            optionLayout.findViewById<TextView>(R.id.optionCategoryName).text = it.optionsCategoryName

            val optionsRadioGroup = optionLayout.findViewById<RadioGroup>(R.id.optionsRadioGroup)
            val addedPriceLayout = optionLayout.findViewById<LinearLayout>(R.id.addedPriceLayout)
            val requiredLabel = optionLayout.findViewById<TextView>(R.id.requiredLabel)

            if (!it.required) {
                requiredLabel.visibility = View.INVISIBLE
            }

            it.options.forEachIndexed { index, option ->
                if (it.singleSelection) {
                    // Add radio button
                    val radioButton = RadioButton(this).apply {
                        text = option.optionName
                        textSize = 18f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            60.px)
                        setOnClickListener { singleSelectionOptionWasSelected(optionsRadioGroup, option, requiredLabel) }
                    }

                    optionsRadioGroup.addView(radioButton)
                } else {
                    // Add check box
                    val checkBox = CheckBox(this).apply {
                        text = option.optionName
                        textSize = 18f
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            60.px)
                        setOnClickListener { multiSelectionOptionWasSelected(this.isChecked, option, requiredLabel) }
                    }

                    optionsRadioGroup.addView(checkBox)
                }

                // Add divider if appropriate
                if (it.options.size > 1 && index != it.options.size - 1) {
                    val divider = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            1.px)
                    }
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.quantum_black_divider))
                    optionsRadioGroup.addView(divider)
                }

                val addedPriceTextView = TextView(this).apply {
                    text = if (option.addedPrice == 0.0) "" else "add ${option.addedPrice.asPriceString}"
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        1f)
                    gravity = Gravity.CENTER_VERTICAL
                }

                addedPriceLayout.addView(addedPriceTextView)
            }

            menuOptionsLayout.addView(optionLayout)
        }
    }

    private fun singleSelectionOptionWasSelected(radioGroup: RadioGroup,
                                                 menuItemOption: MenuItemOption,
                                                 requiredLabel: TextView) {
        requiredLabel.setTextColor(ContextCompat.getColor(this, R.color.menu_item_green_required))
        singleSelectedOptions[radioGroup.id] = menuItemOption

        updateTotalPrice()
    }

    private fun multiSelectionOptionWasSelected(checked: Boolean,
                                                menuItemOption: MenuItemOption,
                                                requiredLabel: TextView) {
        requiredLabel.setTextColor(ContextCompat.getColor(this, R.color.menu_item_green_required))

        if (checked) {
            multiSelectedOptions.add(menuItemOption)
        } else {
            multiSelectedOptions.remove(menuItemOption)
        }

        updateTotalPrice()
    }

    fun decreaseQuantityTapped(v: View) {
        changeQuantity(false)
    }

    fun increaseQuantityTapped(v: View) {
        changeQuantity(true)
    }

    private fun changeQuantity(increase: Boolean) {
        val oldQuantity = quantity.text.toString().toInt()
        val newQuantity = if (increase) oldQuantity + 1 else oldQuantity - 1

        decreaseQuantityButton.isEnabled = newQuantity != 1
        increaseQuantityButton.isEnabled = newQuantity != 10

        quantity.text = newQuantity.toString()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        var totalPrice = menuItem.price

        val multiOptionPrices = multiSelectedOptions.map { it.addedPrice }
        if (multiOptionPrices.isNotEmpty()) {
            totalPrice += multiOptionPrices.reduce { acc, it -> acc + it}
        }

        val singleOptionPrices = singleSelectedOptions.values.map { it.addedPrice }
        if (singleOptionPrices.isNotEmpty()) {
            totalPrice += singleOptionPrices.reduce { acc, it -> acc + it}
        }

        price.text = (totalPrice * quantity.text.toString().toInt()).asPriceString
    }

    fun addToOrder(v: View) {
        val selectedQuantity = quantity.text.toString().toInt()
        val theseItemsThisItem = if (selectedQuantity > 1) "these items" else "this item"

        val currentCartRestaurant = Cart.getRestaurant(this)
        if (currentCartRestaurant != null && Cart.getItems(this).isNotEmpty()) {
            if (restaurant.id != currentCartRestaurant.id) {
                // User is trying to add an item from a different restaurant
                AlertDialog.Builder(this)
                    .setTitle("Clear Cart?")
                    .setMessage("You already have items from ${currentCartRestaurant.name} in your cart. Adding $theseItemsThisItem will create a new cart with $theseItemsThisItem from ${restaurant.name}.")
                    .setNegativeButton("cancel", null)
                    .setPositiveButton("Confirm") {_, _ ->
                        Cart.empty(this)
                        addToOrder(v)
                    }.show()
                return
            }

            if (Cart.getTotalQuantity(this) >= 10) {
                // Cart is full
                AlertDialog.Builder(this)
                    .setTitle("Cart Full")
                    .setMessage("Your cart already has the maximum number of items. Please remove some items or reduce the quantity of this item, then try again.")
                    .setPositiveButton("Okay", null)
                    .show()
                return
            }
        }

        if (UserUtil.getCurrentUser(this)!!.currentOrder != null) {
            // Current order already in progress
            AlertDialog.Builder(this)
                .setTitle("Order Already in Progress")
                .setMessage("You already have an order on its way! Check the Orders tab to see its progress.")
                .setPositiveButton("Okay", null)
                .show()
            return
        }

        var selectedOrderOptions = ""

        val optionsArray = ArrayList(singleSelectedOptions.values.map { it.optionName })
        optionsArray.addAll(multiSelectedOptions.map { it.optionName })
        optionsArray.forEach {
            selectedOrderOptions += "$it, "
        }

        selectedOrderOptions = if (selectedOrderOptions.isEmpty()) "None" else selectedOrderOptions.removeSuffix(", ")

        menuItem.selectedOptions = selectedOrderOptions
        menuItem.selectedQuantity = selectedQuantity
        menuItem.specialInstructions = specialInstructionsEditText.text.toString()
        menuItem.finalPrice = price.text.toString().removePrefix("₱").toDouble()
        Cart.setRestaurant(this, restaurant)
        Cart.addItem(this, menuItem)

        TDUtil.showSuccessDialog(this, "Added to Order") {
            finish()
        }
    }

    override fun onScrollChanged() {
        if (usingImageLayout) {
            if (scrollView.scrollY > 115) {
                if (toolbar_layout.title != menuItem.itemName) {
                    toolbar_layout.title = menuItem.itemName
                    window.statusBarColor = Color.WHITE
                }
            } else {
                if (toolbar_layout.title != "") {
                    toolbar_layout.title = ""
                    window.statusBarColor = Color.TRANSPARENT
                }
            }
        } else {
            if (scrollView.scrollY > 85) {
                if (supportActionBar?.title != menuItem.itemName) {
                    supportActionBar?.title = menuItem.itemName
                    supportActionBar?.elevation = 20f
                }
            } else {
                if (supportActionBar?.title != "") {
                    supportActionBar?.title = ""
                    supportActionBar?.elevation = 0f
                }
            }
        }
    }
}
