package com.griffsoft.tsadadelivery.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity
import com.griffsoft.tsadadelivery.User
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.ui.account.RC_USER_NAME_DID_CHANGE
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_account_details.*


class AccountDetailsActivity : TDActivity() {

    private lateinit var currentUser: User
    private var phoneEntryIsValid: Boolean = false
    private var phoneExtractedValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        currentUser = UserUtil.getCurrentUser(this)!!

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

    fun saveChangesTapped(v: View) {
        nameTextField.clearFocus()
        phoneTextField.clearFocus()

        UserUtil.setName(this, nameTextField.text.toString())

        if (phoneEntryIsValid) {
            UserUtil.setPhone(this, phoneExtractedValue)
        }

        TDUtil.showSuccessDialog(this, "Changes Saved") {
            val returnIntent = Intent()
            returnIntent.putExtra("newName", nameTextField.text.toString())
            setResult(RC_USER_NAME_DID_CHANGE, returnIntent)
            finish()
        }
    }
}