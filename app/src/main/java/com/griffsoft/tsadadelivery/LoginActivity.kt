package com.griffsoft.tsadadelivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

const val RC_SIGN_IN = 0
const val RC_DID_RETURN_TO_LOGIN = 1

@Suppress("LiftReturnOrAssignment")
class LoginActivity : TDActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar!!.hide()

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_sign_in_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    fun googleSignIn(v: View) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loadingViewLayout.visibility = View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
                loadingViewLayout.visibility = View.GONE
            }
        } else if (requestCode == RC_DID_RETURN_TO_LOGIN) {
            Log.v(TAG, "LOGGING OUT")
            auth.currentUser?.let {
                auth.signOut()
            }
            googleSignInClient.signOut()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser!!

                    val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)


                    userDocRef.get().addOnCompleteListener {
                        lateinit var udUser: User

                        if (it.isSuccessful) {
                            val document = it.result!!

                            if (document.exists()) {
                                // User exists
                                udUser = document.toObject(User::class.java)!!
                            } else {
                                // User does not exist, so we should create one
                                val userName = user.displayName ?: ""
                                userDocRef.set(hashMapOf("name" to userName))
                                udUser = User(userName)
                            }

                            UserUtil.updateCurrentUser(this, udUser)

                            loadingViewLayout.visibility = View.GONE

                            if (udUser.addresses.isEmpty()) {
                                val addNewAddressIntent = Intent(this, AddNewAddressSearchViewActivity::class.java)
                                startActivityForResult(addNewAddressIntent, RC_DID_RETURN_TO_LOGIN)
                            } else {

                            }

                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_LONG).show()
                }
            }
    }
}
