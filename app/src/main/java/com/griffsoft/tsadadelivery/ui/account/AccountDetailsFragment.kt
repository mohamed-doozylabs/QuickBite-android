package com.griffsoft.tsadadelivery.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.User
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.TDEditText
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.redmadrobot.inputmask.MaskedTextChangedListener

class AccountDetailsFragment : TDFragment() {
    private lateinit var nameTextField: TDEditText
    private lateinit var phoneTextField: TDEditText
    private lateinit var saveChangesButton: Button

    private lateinit var currentUser: User
    private var phoneEntryIsValid: Boolean = false
    private var phoneExtractedValue: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account_details, container, false)
        setupBackButton(root)

        nameTextField = root.findViewById(R.id.nameTextField)
        phoneTextField = root.findViewById(R.id.phoneTextField)
        saveChangesButton = root.findViewById(R.id.saveChangesButton)
        saveChangesButton.setOnClickListener { saveChangesTapped() }

        currentUser = UserUtil.getCurrentUser(context!!)!!

        nameTextField.setText(currentUser.name)

        if (currentUser.phone.isNotEmpty()) {
            phoneTextField.setText(currentUser.phone)
            phoneEntryIsValid = true
            phoneExtractedValue = currentUser.phone
        }

        nameTextField.addTextChangedListener {
            refreshSaveButton()
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
                        refreshSaveButton()
                    }
                }
            })

        return root
        }

    private fun refreshSaveButton() {
        var nameTextFieldIsValid = false

        if (currentUser.name.isEmpty()) {
            if (nameTextField.text!!.isNotEmpty()) {
                nameTextFieldIsValid = true
            }
        } else {
            if (nameTextField.text!!.toString() != currentUser.name && nameTextField.text!!.isNotEmpty()) {
                nameTextFieldIsValid = true
            }
        }

        val phoneFieldIsValid = phoneEntryIsValid

        val enableSaveButton = nameTextFieldIsValid || phoneFieldIsValid
        saveChangesButton.isEnabled = enableSaveButton
    }

    private fun saveChangesTapped() {
        nameTextField.clearFocus()
        phoneTextField.clearFocus()

        UserUtil.setName(context!!, nameTextField.text.toString())

        if (phoneEntryIsValid) {
            UserUtil.setPhone(context!!, phoneExtractedValue)
        }

        TDUtil.showSuccessDialog(context!!, "Changes Saved") {
            activity?.onBackPressed()
        }
    }

}
