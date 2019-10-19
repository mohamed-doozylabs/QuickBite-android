package com.griffsoft.tsadadelivery.ui.account

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.griffsoft.tsadadelivery.*
import com.griffsoft.tsadadelivery.account.AccountDetailsActivity
import com.griffsoft.tsadadelivery.account.AddressesActivity
import com.griffsoft.tsadadelivery.account.NotificationsActivity
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.get_started.RC_SIGN_IN
import kotlinx.android.synthetic.main.account_menu_list_item.view.*
import timber.log.Timber

const val RC_USER_NAME_DID_CHANGE = 2

class AccountFragment : Fragment(), View.OnClickListener, AccountMenuAdapter.OnItemClickListener {

    private lateinit var accountMenuAdapter: AccountMenuAdapter
    private lateinit var menuItems: ArrayList<String>
    private lateinit var tdTabBarActivity: TDTabBarActivity

    private lateinit var logOutButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var signInLayout: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account, container, false)

        tdTabBarActivity = activity as TDTabBarActivity

        val currentUser = UserUtil.getCurrentUser(context!!)!!

        logOutButton = root.findViewById(R.id.logOutButton)
        googleSignInButton = root.findViewById(R.id.googleSignInButton)
        signInLayout = root.findViewById(R.id.signInLayout)

        updateUI()

        val accountDetailsMenuTitle = if (currentUser.name.isEmpty()) "Account Details" else currentUser.name
        menuItems = arrayListOf("${accountDetailsMenuTitle}|Change your account information",
            "Addresses|Add or remove a delivery address",
            "Notifications")

        accountMenuAdapter = AccountMenuAdapter(menuItems, context!!, this)

        val menuRecyclerView = root.findViewById<RecyclerView>(R.id.accountMenuRecyclerView)
        menuRecyclerView.layoutManager = LinearLayoutManager(context!!)
        menuRecyclerView.adapter = accountMenuAdapter

        return root
    }

    override fun accountMenuItemWasSelected(position: Int) {
        when (position) {
            0 -> startActivityForResult(Intent(activity, AccountDetailsActivity::class.java), RC_USER_NAME_DID_CHANGE)
            1 -> startActivity(Intent(activity, AddressesActivity::class.java))
            2 -> startActivity(Intent(activity, NotificationsActivity::class.java))
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.logOutButton -> logOut()
            R.id.googleSignInButton -> googleSignIn()
        }
    }

    private fun updateUI() {
        if (UserUtil.getCurrentUser(context!!)!!.isGuest) {
            logOutButton.visibility = View.GONE
            signInLayout.visibility = View.VISIBLE
            googleSignInButton.setOnClickListener(this)
        } else {
            logOutButton.visibility = View.VISIBLE
            signInLayout.visibility = View.GONE
            logOutButton.setOnClickListener(this)
        }
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
                //TODO: Empty cart

                UserUtil.clearCurrentUser(context!!)

                //TODO: Facebook Logout

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

        if (requestCode == RC_SIGN_IN) {
            tdTabBarActivity.showLoadingCoverView(true)
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w(e, "Google sign in failed")
                tdTabBarActivity.showLoadingCoverView(false)
            }
        } else if (resultCode == RC_USER_NAME_DID_CHANGE) {
            updateNameMenuItem(data!!.getStringExtra("newName"))
        }
    }

    private fun updateNameMenuItem(newName: String) {
        menuItems[0] = "$newName|Change your account information"
        accountMenuAdapter.notifyDataSetChanged()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = auth.currentUser!!

                    val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

                    userDocRef.get().addOnCompleteListener {
                        val currentUdUser = UserUtil.getCurrentUser(context!!)!!

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
                                }
                            } else {
                                // MAIN CASE: User does not have an existing Firebase user.
                                // Create one from the existing information in their guest account

                                // If user has not set a name for their guest account, use the name from their social media.
                                // If that fails, remain blank.
                                currentUdUser.name = if (currentUdUser.name.isNotEmpty()) currentUdUser.name else user.displayName ?: ""
                                currentUdUser.isGuest = false
                                UserUtil.updateCurrentUser(context!!, currentUdUser)

                                userDocRef.set(currentUdUser)
                                updateNameMenuItem(currentUdUser.name)
                                updateUI()
                                tdTabBarActivity.showLoadingCoverView(false)
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w(task.exception, "signInWithCredential:failure")
                }
            }
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
                    itemClickListener.accountMenuItemWasSelected(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun accountMenuItemWasSelected(position: Int)
    }
}

