package com.griffsoft.tsadadelivery.get_started

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.*
import com.griffsoft.tsadadelivery.extras.TDUtil
import kotlinx.android.synthetic.main.activity_get_started.*
import timber.log.Timber
import java.util.*

const val RC_SIGN_IN = 555
const val RC_DID_RETURN_TO_LOGIN = 556

@Suppress("LiftReturnOrAssignment")
class LoginActivity : TDActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)
        supportActionBar!!.hide()

        auth = FirebaseAuth.getInstance()

        googleSignInClient = GoogleSignIn.getClient(this, TDUtil.gso)
    }

    fun facebookSignIn(v: View) {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    authenticateWithFirebase(FacebookAuthProvider.getCredential(result!!.accessToken.token))
                }

                override fun onCancel() {
                    Timber.d("❤️ OnFacebookCancel")
                    Handler().post {
                        loadingViewLayout.visibility = View.GONE
                    }
                }

                override fun onError(error: FacebookException?) {
                    Timber.d("❤️ OnFacebookError")
                    Handler().post {
                        loadingViewLayout.visibility = View.GONE
                    }
                }
            })
    }

    fun googleSignIn(v: View) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (callbackManager != null) {
            loadingViewLayout.visibility = View.VISIBLE
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loadingViewLayout.visibility = View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                authenticateWithFirebase(GoogleAuthProvider.getCredential(account!!.idToken, null))
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w(e, "Google sign in failed")
                loadingViewLayout.visibility = View.GONE
            }
        } else if (requestCode == RC_DID_RETURN_TO_LOGIN) {
            Timber.v("LOGGING OUT")
            auth.currentUser?.let {
                auth.signOut()
            }
            googleSignInClient.signOut()
        }
    }

    private fun authenticateWithFirebase(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
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
                                userDocRef.set(hashMapOf("userName" to userName))
                                udUser = User(userName)
                            }

                            UserUtil.updateCurrentUser(this, udUser)

                            loadingViewLayout.visibility = View.GONE

                            if (udUser.addresses.isEmpty()) {
                                segueToAddNewAddressSearch()
                            } else {
                                val homeScreenIntent = Intent(this, TDTabBarActivity::class.java)
                                homeScreenIntent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(homeScreenIntent)
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.e("❤️ signInWithCredential:failure")
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun segueToAddNewAddressSearch() {
        val addNewAddressIntent = Intent(this, AddNewAddressSearchActivity::class.java)
        startActivityForResult(addNewAddressIntent, RC_DID_RETURN_TO_LOGIN)
    }

    fun continueWithoutAccountTapped(v: View) {
        UserUtil.updateCurrentUser(
            this,
            User(isGuest = true)
        )
        segueToAddNewAddressSearch()
    }
}
