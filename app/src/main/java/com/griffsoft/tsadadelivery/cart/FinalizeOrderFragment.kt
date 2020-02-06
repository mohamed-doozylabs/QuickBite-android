package com.griffsoft.tsadadelivery.cart


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.TDEditText
import com.redmadrobot.inputmask.MaskedTextChangedListener

@SuppressLint("SetTextI18n")
class FinalizeOrderFragment : TDFragment() {

    private lateinit var nameTextInputLayout: TextInputLayout
    private lateinit var nameTextField: TDEditText
    private lateinit var phoneTextInputLayout: TextInputLayout
    private lateinit var phoneTextField: TDEditText
    private lateinit var continueButton: Button

    private var phoneEntryIsValid = false
    private var phoneExtractedValue: String = ""

    private var finalizeOrderMode = false

    var namedPassedFromReviewAndPlace = ""
    var phonePassedFromReviewAndPlace = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_finalize_order, container, false)

        val header = root.findViewById<TextView>(R.id.finalize)

        nameTextInputLayout = root.findViewById(R.id.nameTextInputLayout)
        nameTextField = root.findViewById(R.id.nameTextField)
        phoneTextInputLayout = root.findViewById(R.id.phoneTextInputLayout)
        phoneTextField = root.findViewById(R.id.phoneTextField)
        continueButton = root.findViewById(R.id.continueButton)
        continueButton.setOnClickListener { continueTapped() }

        val currentUser = UserUtil.getCurrentUser(context!!)!!

        finalizeOrderMode = arguments?.getBoolean("finalizeOrderMode") ?: false

        if (finalizeOrderMode) {
            header.text = "Finalize your order from ${Cart.getRestaurant(context!!)!!.name}"

            if (currentUser.name.isNotEmpty()) {
                nameTextInputLayout.visibility = View.GONE
            }

            if (currentUser.phone.isNotEmpty()) {
                phoneTextInputLayout.visibility = View.GONE
            }
        } else {
            header.text = "Contact Info"
            continueButton.text = "Save"
            nameTextField.setText(namedPassedFromReviewAndPlace)
            phoneTextField.setText(phonePassedFromReviewAndPlace)
            phoneEntryIsValid = true
            continueButton.isEnabled = true
        }

        nameTextField.addTextChangedListener {
            refreshContinueButton()
        }

        MaskedTextChangedListener.installOn(
            phoneTextField,
            "+63 [000] [000] [0000]",
            object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(
                    maskFilled: Boolean,
                    extractedValue: String,
                    formattedValue: String
                ) {
                    phoneEntryIsValid = maskFilled
                    if (phoneExtractedValue != formattedValue) {
                        phoneExtractedValue = formattedValue
                        refreshContinueButton()
                    }
                }
            })

        setupBackButton(root, arrayListOf(nameTextField, phoneTextField))
        return root
    }

    private fun refreshContinueButton() {
        var fieldsAreValid = true

        if (nameTextInputLayout.visibility != View.GONE) {
            if (nameTextField.text.toString().isEmpty()) {
                fieldsAreValid = false
            }
        }

        if (phoneTextInputLayout.visibility != View.GONE) {
            if (!phoneEntryIsValid) {
                fieldsAreValid = false
            }
        }

        continueButton.isEnabled = fieldsAreValid
    }

    private fun continueTapped() {
        if (finalizeOrderMode) {
            val bundle = bundleOf()

            if (nameTextInputLayout.visibility != View.GONE) {
                bundle.putString("name", nameTextField.text.toString())
            }

            if (phoneTextInputLayout.visibility != View.GONE) {
                bundle.putString("phone", phoneExtractedValue)
            }

            performSegue(R.id.action_finalizeOrderFragment_to_reviewAndPlaceOrderFragment, bundle)
        } else {
            val returnIntent = Intent()
            returnIntent.putExtra("name", nameTextField.text.toString())

            if (phoneExtractedValue.isNotEmpty()) {
                returnIntent.putExtra("phone", phoneExtractedValue)
            } else {
                returnIntent.putExtra("phone", phonePassedFromReviewAndPlace)
            }
            activity!!.setResult(RC_UPDATE_CONTACT_INFO, returnIntent)
            activity!!.finish()
        }


    }

}
