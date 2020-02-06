package com.griffsoft.tsadadelivery.ui.orders


import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.asPriceString
import com.griffsoft.tsadadelivery.cart.CartAdapter
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.objects.Order
import com.squareup.picasso.Picasso
import timber.log.Timber

@SuppressLint("SetTextI18n", "InflateParams")
class CurrentOrderFragment : TDFragment() {

    private lateinit var progressBarForeground: View

    private var lottieCheckboxes = arrayListOf<LottieAnimationView>()
    private var orderStageTitles = arrayListOf<TextView>()
    private var orderStageSubtitles = arrayListOf<TextView>()

    private val progressBarStageValues = arrayListOf(0f, 0.33f, 0.66f, 1f)

    private var currentStage: Int = 0
    private lateinit var currentOrder: Order
    private var currentOrderListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (UserUtil.getCurrentUser(context!!)!!.currentOrder == null) {
            val options = NavOptions.Builder().setPopUpTo(R.id.currentOrderFragment, true).build()
            findNavController().navigate(R.id.action_currentOrderFragment_to_pastOrdersFragment, null, options)
            return null
        }

        val root =  inflater.inflate(R.layout.fragment_current_order, container, false)

        currentOrder = UserUtil.getCurrentUser(context!!)!!.currentOrder!!
        currentStage = currentOrder.currentStage

        Picasso.get()
            .load(currentOrder.restaurantImageUrl)
            .placeholder(ContextCompat.getDrawable(context!!, R.drawable.placeholder)!!)
            .into(root.findViewById<ImageView>(R.id.restaurantImage))

        root.findViewById<TextView>(R.id.restaurantName).text = currentOrder.restaurantName
        val s = if (currentOrder.items.size > 1) "s" else ""
        root.findViewById<TextView>(R.id.orderDetails).text = "${currentOrder.items.size} item$s · Total ${currentOrder.total.asPriceString}"

        root.findViewById<Button>(R.id.seeDetailsButton).setOnClickListener { showOrderDetails() }

        progressBarForeground = root.findViewById(R.id.progressBarForeground)

        // If we navigate to past orders screen, this class will not be garbage collected,
        // so the array will remain populated and when the user returns, it will add more
        // textviews
        lottieCheckboxes = arrayListOf(
            root.findViewById(R.id.stage1CheckboxAnimationView),
            root.findViewById(R.id.stage2CheckboxAnimationView),
            root.findViewById(R.id.stage3CheckboxAnimationView),
            root.findViewById(R.id.stage4CheckboxAnimationView)
        )

        orderStageTitles = arrayListOf(
            root.findViewById(R.id.orderSubmitted),
            root.findViewById(R.id.beingPreparedByStore),
            root.findViewById(R.id.orderOnItsWay),
            root.findViewById(R.id.foodDelivered)
        )

        val beingPreparedByStoreSubtitle = root.findViewById<TextView>(R.id.beingPreparedByStoreSubtitle).apply {
            text = "${currentOrder.restaurantName} is preparing your order."
        }

        orderStageSubtitles = arrayListOf(
            root.findViewById(R.id.orderSubmittedSubtitle),
            beingPreparedByStoreSubtitle,
            root.findViewById(R.id.orderOnItsWaySubtitle),
            root.findViewById(R.id.foodDeliveredSubtitle)
        )

        setUiForInitialStage()

        root.findViewById<Button>(R.id.pastOrdersButton).setOnClickListener {
            performSegue(R.id.action_currentOrderFragment_to_pastOrdersFragment, bundleOf("navigatedToFromCurrentOrder" to true))
        }

        root.findViewById<Button>(R.id.contactUsButton).setOnClickListener {
            UserUtil.moveCurrentOrderToPreviousOrders(context!!)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        setupOrderListener()
    }

    // Warning: putting this code in onCreateView causes a crash the second time the tab
    // is opened...no idea why. So just leave it here.
    private fun setupOrderListener() {
        if (currentOrderListener == null) {
            val orderRef =
                FirebaseFirestore.getInstance().collection("orders").document(currentOrder.id)

            currentOrderListener = orderRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Timber.w(e, "Listen failed.")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Save latest order info to currentUser
                    currentOrder = snapshot.toObject(Order::class.java)!!
                    // If currentStage is 3 then TDTabBarActivity will update the current order
                    if (currentOrder.currentStage != 3) {
                        UserUtil.addOrUpdateCurrentOrder(context!!, currentOrder)
                    }
                    updateUiIfNeeded()
                } else {
                    Timber.d("Current data: null")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        currentOrderListener?.remove()
    }

    private fun updateUiIfNeeded() {
        if (currentStage != currentOrder.currentStage) {
            // Update UI
            setStageTo(currentOrder.currentStage)
        }
    }

    private fun setUiForInitialStage() {
        progressBarForeground.scaleY = progressBarStageValues[currentStage]

        // Set any completed stages
        for (i in 0 until currentStage) {
            TextViewCompat.setTextAppearance(orderStageTitles[i], R.style.CompletedOrderStage)
            orderStageSubtitles[i].visibility = View.INVISIBLE
            lottieCheckboxes[i].progress = 1f
        }

        // Set current stage
        TextViewCompat.setTextAppearance(orderStageTitles[currentStage], R.style.CurrentOrderStage)
        orderStageSubtitles[currentStage].visibility = View.VISIBLE
        lottieCheckboxes[currentStage].progress = 1f
    }


    private fun setStageTo(newStage: Int) {
        TextViewCompat.setTextAppearance(orderStageTitles[currentStage], R.style.CompletedOrderStage)
        orderStageSubtitles[currentStage].visibility = View.INVISIBLE

        // Check if we're jumping multiple stages
        while (newStage - currentStage > 1) {
            currentStage += 1
            TextViewCompat.setTextAppearance(orderStageTitles[currentStage], R.style.CompletedOrderStage)
            orderStageSubtitles[currentStage].visibility = View.INVISIBLE
            lottieCheckboxes[currentStage].progress = 1f
        }

        progressBarForeground
            .animate()
            .setStartDelay(0)
            .scaleY(progressBarStageValues[newStage])
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(600)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
                override fun onAnimationEnd(p0: Animator?) {
                    lottieCheckboxes[newStage].playAnimation()
                    TextViewCompat.setTextAppearance(orderStageTitles[newStage], R.style.CurrentOrderStage)
                    orderStageSubtitles[newStage].visibility = View.VISIBLE
                }
            })
            .start()

        currentStage = newStage
    }

    private fun showOrderDetails() {
        val orderInfoBottomSheet = BottomSheetDialog(context!!)
        val bottomSheetView = LayoutInflater.from(context!!).inflate(R.layout.current_order_info_bottom_sheet, null)

        TDUtil.populateAddressFields(
            currentOrder.deliveryAddress,
            bottomSheetView.findViewById(R.id.addressName),
            bottomSheetView.findViewById(R.id.unitAndStreet),
            bottomSheetView.findViewById(R.id.buildingLandmark),
            bottomSheetView.findViewById(R.id.instructions)
        )

        bottomSheetView.findViewById<RecyclerView>(R.id.orderItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = CartAdapter(context!!, currentOrder.items, null, true)
        }
        bottomSheetView.findViewById<TextView>(R.id.orderTotal).text = currentOrder.total.asPriceString
        bottomSheetView.findViewById<TextView>(R.id.paymentMethod).text = currentOrder.paymentMethod

        orderInfoBottomSheet.setContentView(bottomSheetView)
        orderInfoBottomSheet.show()
    }
}