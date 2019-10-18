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
//
//    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, activity: Activity) {
//        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(activity) { task ->
//                if (task.isSuccessful) {
//                    Timber.d("signInWithCredential:success")
//                    val user = auth.currentUser!!
//
//                    val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
//
//
//                    userDocRef.get().addOnCompleteListener {
//                        lateinit var udUser: User
//
//                        if (it.isSuccessful) {
//                            val document = it.result!!
//
//                            if (document.exists()) {
//                                // User exists
//                                udUser = document.toObject(User::class.java)!!
//                            } else {
//                                // User does not exist, so we should create one
//                                val userName = user.displayName ?: ""
//                                userDocRef.set(hashMapOf("name" to userName))
//                                udUser = User(userName)
//                            }
//
//                            UserUtil.updateCurrentUser(this, udUser)
//
//                            loadingViewLayout.visibility = View.GONE
//
//                            if (udUser.addresses.isEmpty()) {
//                                segueToAddNewAddressSearch()
//                            } else {
//                                // TODO: Open main screen
//                            }
//                        }
//                    }
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Timber.w(task.exception, "signInWithCredential:failure")
//                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_LONG).show()
//                }
//            }
//    }

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
            callback()
        }
    }
}