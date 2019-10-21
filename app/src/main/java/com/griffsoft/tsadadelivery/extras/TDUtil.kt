package com.griffsoft.tsadadelivery.extras

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
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

    fun showSuccessDialog(context: Context, message: String, showFor: Long = 1000, callback: () -> Unit) {
        val successDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        successDialogView.message.text = message

        val ad = AlertDialog.Builder(context)
            .setView(successDialogView)
            .create()

        ad.setCancelable(false)
        ad.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ad.window?.setDimAmount(0.2f)
        ad.show()

        Timer("successDialogTimer", false).schedule(showFor) {
            ad.dismiss()
            callback()
        }
    }
}