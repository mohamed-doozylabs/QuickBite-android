package com.griffsoft.tsadadelivery.ui.account

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.*
import com.griffsoft.tsadadelivery.cart.Cart
import com.griffsoft.tsadadelivery.extras.OnItemClickListener
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.get_started.RC_SIGN_IN
import kotlinx.android.synthetic.main.account_menu_list_item.view.*
import timber.log.Timber
import java.util.*

class AccountFragment : TDFragment(), View.OnClickListener, OnItemClickListener {

    private lateinit var accountMenuAdapter: AccountMenuAdapter
    private lateinit var menuItems: ArrayList<String>
    private lateinit var tdTabBarActivity: TDTabBarActivity

    private lateinit var logOutButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var facebookSignInButton: Button
    private lateinit var signInLayout: ConstraintLayout

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tdTabBarActivity = activity as TDTabBarActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account, container, false)

        val currentUser = UserUtil.getCurrentUser(context!!)!!

        logOutButton = root.findViewById(R.id.logOutButton)
        googleSignInButton = root.findViewById(R.id.googleSignInButton)
        facebookSignInButton = root.findViewById(R.id.facebookSignInButton)
        signInLayout = root.findViewById(R.id.signInLayout)

        updateUI()

        val accountDetailsMenuTitle = if (currentUser.name.isEmpty()) "Account Details" else currentUser.name
        menuItems = arrayListOf("${accountDetailsMenuTitle}|Change your account information",
            "Addresses|Add or remove a delivery address",
            "Notifications")

        accountMenuAdapter = AccountMenuAdapter(menuItems, context!!, this)

        root.findViewById<RecyclerView>(R.id.accountMenuRecyclerView).apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = accountMenuAdapter
        }

        return root
    }

    override fun onResume() {
        val updatedUserName = UserUtil.getCurrentUser(context!!)!!.name
        if (updatedUserName.isNotEmpty()) {
            updateNameMenuItem(updatedUserName)
        }
        super.onResume()
    }

    override fun itemWasSelected(position: Int, viewHolder: RecyclerView.ViewHolder?) {
        when (position) {
            0 -> performSegue(R.id.action_account_to_accountDetailsFragment)
            1 -> performSegue(R.id.action_account_to_addressesFragment, bundleOf("settingsMode" to true))
            2 -> performSegue(R.id.action_account_to_notificationsFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.logOutButton -> logOut()
            R.id.facebookSignInButton -> facebookSignIn()
            R.id.googleSignInButton -> googleSignIn()
        }
    }

    private fun updateUI() {
        if (UserUtil.getCurrentUser(context!!)!!.isGuest) {
            logOutButton.visibility = View.GONE
            signInLayout.visibility = View.VISIBLE
            googleSignInButton.setOnClickListener(this)
            facebookSignInButton.setOnClickListener(this)
        } else {
            logOutButton.visibility = View.VISIBLE
            signInLayout.visibility = View.GONE
            logOutButton.setOnClickListener(this)
        }
    }

    private fun facebookSignIn() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Timber.d("❤️ authenticateWithFirebase")
                    authenticateWithFirebase(FacebookAuthProvider.getCredential(result!!.accessToken.token))
                }

                override fun onCancel() {
                    Timber.d("❤️ OnFacebookCancel")
                    Handler().post {
                        tdTabBarActivity.showLoadingCoverView(false)
                    }
                }

                override fun onError(error: FacebookException?) {
                    Timber.d("❤️ OnFacebookError")
                    Handler().post {
                        tdTabBarActivity.showLoadingCoverView(false)
                    }
                }
            })
    }

    private fun googleSignIn() {
        val signInIntent = GoogleSignIn.getClient(activity!!, TDUtil.gso).signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun logOut() {
        AlertDialog.Builder(context!!)
            .setTitle("Log Out?")
            .setNegativeButton("cancel", null)
            .setPositiveButton("log out") {_, _ ->
                Cart.empty(context!!)

                UserUtil.clearCurrentUser(context!!)

                if (AccessToken.isCurrentAccessTokenActive()) {
                    LoginManager.getInstance().logOut()
                }

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val googleSignIn = GoogleSignIn.getClient(context!!, gso)
                googleSignIn.signOut()

                FirebaseAuth.getInstance().signOut()

                val appContainerIntent = Intent(context!!, AppContainerActivity::class.java)
                appContainerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(appContainerIntent)
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (callbackManager != null) {
            Handler().post {
                tdTabBarActivity.showLoadingCoverView(true)
            }
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }

        if (requestCode == RC_SIGN_IN) {
            Handler().post {
                tdTabBarActivity.showLoadingCoverView(true)
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                authenticateWithFirebase(GoogleAuthProvider.getCredential(account!!.idToken, null))
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w(e, "Google sign in failed")
                Handler().post {
                    tdTabBarActivity.showLoadingCoverView(false)
                }
            }
        }
    }

    private fun authenticateWithFirebase(credential: AuthCredential) {
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = auth.currentUser!!

                    val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

                    userDocRef.get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            val document = it.result!!

                            if (document.exists()) {
                                // EDGE CASE: The user has signed in with social media account before. This should be a rare case
                                val firebaseUser = document.toObject(User::class.java)!!
                                UserUtil.mergeCurrentUserWithFirebaseUserAndSync(context!!, firebaseUser, user.uid) {
                                    // Present fresh stack?
                                    updateNameMenuItem(UserUtil.getCurrentUser(context!!)!!.name)
                                    updateUI()
                                    tdTabBarActivity.showLoadingCoverView(false)
                                    Toast.makeText(context!!, "Successfully logged in",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } else {

                                // MAIN CASE: User does not have an existing Firebase user.
                                // Create one from the existing information in their guest account

                                // If user has not set a name for their guest account, use the name from their social media.
                                // If that fails, remain blank.
                                val currentUdUser = UserUtil.getCurrentUser(context!!)!!
                                if (currentUdUser.name.isEmpty()) {
                                    currentUdUser.name = user.displayName ?: ""
                                }
                                currentUdUser.isGuest = false
                                UserUtil.updateCurrentUser(context!!, currentUdUser)

                                userDocRef.set(currentUdUser)
                                updateNameMenuItem(currentUdUser.name)
                                updateUI()
                                tdTabBarActivity.showLoadingCoverView(false)
                                Toast.makeText(context!!, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w(task.exception, "signInWithCredential:failure")
                }
            }
    }

    private fun updateNameMenuItem(newName: String) {
        menuItems[0] = "$newName|Change your account information"
        accountMenuAdapter.notifyDataSetChanged()
    }
}

class AccountMenuAdapter(private val items: ArrayList<String>,
                         val context: Context,
                         val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<AccountMenuAdapter.AccountMenuItemViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountMenuItemViewHolder {
        return AccountMenuItemViewHolder(LayoutInflater.from(context).inflate(R.layout.account_menu_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: AccountMenuItemViewHolder, position: Int) {
        val menuItem = items[position]

        holder.menuTitle.text = menuItem.substringBefore("|")

        val subtitle = menuItem.substringAfter("|", "")
        if (subtitle.isEmpty()) {
            holder.menuSubtitle.visibility = View.GONE
        } else {
            holder.menuSubtitle.text = subtitle
        }
    }

    inner class AccountMenuItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menuTitle: TextView = view.menuTitle
        val menuSubtitle: TextView = view.menuSubtitle

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.itemWasSelected(adapterPosition)
                }
            }
        }
    }
}

