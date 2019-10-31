package com.griffsoft.tsadadelivery.extras

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.griffsoft.tsadadelivery.R
import kotlinx.android.synthetic.main.dialog_success.view.*
import java.util.*
import kotlin.concurrent.schedule

object TDUtil {

    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("317568236888-rokn28jtm418qktt99rcaktqsvre5rvn.apps.googleusercontent.com")
        .requestEmail()
        .build()

    fun showSuccessDialog(context: Context, message: String, showFor: Long = 1500, callback: () -> Unit) {
        val successDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        successDialogView.message.text = message

        val ad = AlertDialog.Builder(context)
            .setView(successDialogView)
            .create()

        ad.setCancelable(false)
        ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ad.window?.setDimAmount(0.3f)
        ad.show()

        Timer("successDialogTimer", false).schedule(showFor) {
            ad.dismiss()
            callback()
        }
    }

    fun sharedPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(context.getString(R.string.SHARED_PREFS_KEY), Context.MODE_PRIVATE)
    }

    fun getSharedPrefsString(context: Context, key: Int): String {
        val sharedPrefs = sharedPrefs(context)
        return sharedPrefs.getString(context.getString(key), "")!!
    }

    fun isNetworkReachable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}

interface OnItemClickListener {
    fun itemWasSelected(position: Int, viewHolder: RecyclerView.ViewHolder? = null)
}