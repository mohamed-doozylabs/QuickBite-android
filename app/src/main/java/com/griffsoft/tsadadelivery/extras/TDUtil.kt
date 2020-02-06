package com.griffsoft.tsadadelivery.extras

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.objects.Address
import kotlinx.android.synthetic.main.dialog_success.view.*
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("SetTextI18n")
object TDUtil {

    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("317568236888-rokn28jtm418qktt99rcaktqsvre5rvn.apps.googleusercontent.com")
        .requestEmail()
        .build()

    fun showSuccessDialog(context: Context,
                          message: String,
                          showFor: Long = 1500,
                          callback: () -> Unit) {
        val successDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        successDialogView.message.text = message

        val ad = AlertDialog.Builder(context)
            .setView(successDialogView)
            .create()

        ad.setCancelable(false)
        ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ad.window?.setDimAmount(0.35f)
        ad.show()

        Timer("successDialogTimer", false).schedule(showFor) {
            ad.dismiss()
            callback()
        }
    }

    fun sharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.SHARED_PREFS_KEY), Context.MODE_PRIVATE)
    }

    fun getSharedPrefsString(context: Context, key: Int): String {
        val sharedPrefs = sharedPrefs(context)
        return sharedPrefs.getString(context.getString(key), "") ?: ""
    }

    fun isNetworkReachable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    fun updateNotificationToken(context: Context, token: String) {
        with (sharedPrefs(context).edit()) {
            putString(context.getString(R.string.SHARED_PREFS_KEY_NOTIFICATION_TOKEN), token)
            apply()
        }
    }

    fun getNotificationToken(context: Context): String {
        return getSharedPrefsString(context, R.string.SHARED_PREFS_KEY_NOTIFICATION_TOKEN)
    }


    fun populateAddressFields(address: Address,
                              addressNameTextView: TextView,
                              unitAndStreetTextView: TextView,
                              buildingLandmarkTextView: TextView,
                              instructionsTextView: TextView) {

        addressNameTextView.text = address.displayName

        if (address.floorDoorUnitNo.isNotEmpty()) {
            if (address.userNickname.isNotEmpty()) {
                // First line is set to user nickname, so append street
                unitAndStreetTextView.text = address.floorDoorUnitNo + ", " + address.street
            } else {
                unitAndStreetTextView.text = address.floorDoorUnitNo
            }
        } else if (address.userNickname.isEmpty()) {
            // No unit or floor set, but there is a userNickname
            unitAndStreetTextView.text = address.street
        } else {
            unitAndStreetTextView.visibility = View.GONE
        }

        if (address.buildingLandmark.isNotEmpty()) {
            buildingLandmarkTextView.text = address.buildingLandmark
        } else {
            buildingLandmarkTextView.visibility = View.GONE
        }

        if (address.instructions.isNotBlank()) {
            instructionsTextView.text  = "Instructions: " + address.instructions
        } else {
            instructionsTextView.visibility = View.GONE
        }
    }
}

interface OnItemClickListener {
    fun itemWasSelected(position: Int, viewHolder: RecyclerView.ViewHolder? = null)
}