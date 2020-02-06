package com.griffsoft.tsadadelivery

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.extras.TDEditText

@SuppressLint("SetTextI18n")
class OrderFeedbackDialog : DialogFragment() {

    private lateinit var orderId: String
    private lateinit var restaurantName: String

    private lateinit var ratingBar: RatingBar
    private lateinit var ratingTextView: TextView
    private var ratingTextViewOriginalColor: ColorStateList? = null
    private lateinit var feedbackInput: TDEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orderId = arguments!!.getString("orderId")!!
        restaurantName = arguments!!.getString("restaurantName")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dialog_order_feedback, container, false)

        rootView.findViewById<TextView>(R.id.feedbackTitle).text = "How was your experience ordering from $restaurantName?"
        rootView.findViewById<Button>(R.id.submitFeedbackButton).setOnClickListener { submitFeedback() }
        rootView.findViewById<Button>(R.id.skipButton).setOnClickListener { skipFeedback() }

        ratingTextView = rootView.findViewById(R.id.ratingTextView)

        ratingBar = rootView.findViewById(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            // Restore original text color if necessary
            ratingTextViewOriginalColor?.let {
                ratingTextView.setTextColor(it)
            }
            when (rating) {
                1f -> ratingTextView.text = "Hated it"
                2f -> ratingTextView.text = "Disliked it"
                3f -> ratingTextView.text = "Could use improvement"
                4f -> ratingTextView.text = "Liked it"
                5f -> ratingTextView.text = "Loved it!"
            }
        }

        feedbackInput = rootView.findViewById(R.id.feedbackInput)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val params:ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = LinearLayout.LayoutParams.MATCH_PARENT
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as android.view.WindowManager.LayoutParams
    }

    private fun submitFeedback() {
        if (ratingBar.rating == 0f) {
            // user didn't select a rating
            ratingTextViewOriginalColor = ratingTextView.textColors
            ratingTextView.text = "Please select a rating"
            ratingTextView.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("feedback")
            .document(orderId)
            .set(hashMapOf(
                "orderId" to orderId,
                "feedbackRating" to ratingBar.rating,
                "feedbackInput" to feedbackInput.text.toString()
            ))
        UserUtil.moveCurrentOrderToPreviousOrders(context!!)
        dismiss()
    }

    private fun skipFeedback() {
        UserUtil.moveCurrentOrderToPreviousOrders(context!!)
        dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance(orderId: String, restaurantName: String) =
            OrderFeedbackDialog().apply {
                arguments = Bundle().apply {
                    putString("orderId", orderId)
                    putString("restaurantName", restaurantName)
                }
            }
    }
}