package com.griffsoft.tsadadelivery

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.griffsoft.tsadadelivery.objects.Address
import com.griffsoft.tsadadelivery.objects.Order
import timber.log.Timber

@Suppress("EnumEntryName")
enum class SyncProperty {
    customerName,
    customerPhone,
    addresses,
    pastOrders,
    pushNotificationsEnabled,
    smsNotificationsEnabled
}

object UserUtil {
    private val gson = Gson()
    private val dbUsers = FirebaseFirestore.getInstance().collection("users")

    fun updateCurrentUser(context: Context, user: User) {
        val sharedPref = context.applicationContext.getSharedPreferences(context.getString(R.string.SHARED_PREFS_KEY), Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            val userData = gson.toJson(user)
            putString(context.getString(R.string.SHARED_PREFS_KEY_CURRENT_USER), userData)
            apply()
        }
    }

    fun getCurrentUser(context: Context): User? {
        val sharedPref = context.applicationContext.getSharedPreferences(context.getString(R.string.SHARED_PREFS_KEY), Context.MODE_PRIVATE)
        val currentUserData = sharedPref.getString(context.getString(R.string.SHARED_PREFS_KEY_CURRENT_USER), "")
        return if (currentUserData != "") {
            gson.fromJson(currentUserData, User::class.java)
        } else {
            null
        }
    }

    fun clearCurrentUser(context: Context) {
        val sharedPref = context.applicationContext.getSharedPreferences(context.getString(R.string.SHARED_PREFS_KEY), Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            remove(context.getString(R.string.SHARED_PREFS_KEY_CURRENT_USER))
            apply()
        }
    }

    // Used when someone using a guest account logs in with their google/fb account,
    // but they already have
    fun mergeCurrentUserWithFirebaseUserAndSync(context: Context,
                                                firebaseUser: User,
                                                firebaseUserId: String,
                                                callback: () -> Unit) {
        val mergedUser = getCurrentUser(context)!!

        mergedUser.name = firebaseUser.name

        firebaseUser.addresses.forEach {
            it.isDefault = false
            it.isSelected = false
        }
        mergedUser.addresses.addAll(firebaseUser.addresses)

        mergedUser.isGuest = false

        updateCurrentUser(context, mergedUser)
        dbUsers.document(firebaseUserId).set(mergedUser).addOnCompleteListener {
            callback()
        }
    }

    fun setName(context: Context, name: String) {
        val user = getCurrentUser(context)!!
        if (user.name == name) { return }

        user.name = name
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.customerName)
    }

    fun setPhone(context: Context, phone: String) {
        val user = getCurrentUser(context)!!
        if (user.phone == phone) { return }

        user.phone = phone
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.customerPhone)
    }

    fun addCurrentOrder(context: Context, order: Order) {
        val user = getCurrentUser(context)!!
        user.currentOrder = order
        updateCurrentUser(context, user)
    }

    fun moveCurrentOrderToPreviousOrders(context: Context) {
        val user = getCurrentUser(context)!!
        val currentOrder = user.currentOrder ?: return
        user.pastOrders.add(currentOrder)
        user.currentOrder = null
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.pastOrders)

    }

    fun addAddress(context: Context, newAddress: Address) {
        val user = getCurrentUser(context)!!

        if (newAddress.isDefault) {
            user.addresses.forEach { it.isDefault = false }
        }

        if (newAddress.isSelected) {
            user.addresses.forEach { it.isSelected = false }
        }

        user.addresses.add(newAddress)
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.addresses)
    }

    fun setDefaultAddress(context: Context, addressId: String) {
        val user = getCurrentUser(context)!!

        user.addresses.forEach { it.isDefault = (it.id == addressId) }
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.addresses)
    }

    fun setSelectedAddress(context: Context, addressId: String) {
        val user = getCurrentUser(context)!!

        user.addresses.forEach { it.isSelected = (it.id == addressId) }
        updateCurrentUser(context, user)
    }

    fun setPushNotificationsEnabled(context: Context, enable: Boolean) {
        val user = getCurrentUser(context)!!

        if (user.pushNotificationsEnabled == enable) return

        user.pushNotificationsEnabled = enable
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.pushNotificationsEnabled)
    }

    fun setSmsNotificationsEnabled(context: Context, enable: Boolean) {
        val user = getCurrentUser(context)!!

        if (user.smsNotificationsEnabled == enable) return

        user.smsNotificationsEnabled = enable
        updateCurrentUser(context, user)
        syncUserProperty(context, SyncProperty.smsNotificationsEnabled)
    }

    private fun syncUserProperty(context: Context, property: SyncProperty) {
        val fbUser = FirebaseAuth.getInstance().currentUser ?: return

        val user = getCurrentUser(context)!!

        val newValue: Any = when (property) {
            SyncProperty.customerName -> user.name
            SyncProperty.customerPhone -> user.phone
            SyncProperty.addresses -> user.addresses
            SyncProperty.pastOrders -> user.pastOrders
            SyncProperty.pushNotificationsEnabled -> user.pushNotificationsEnabled
            SyncProperty.smsNotificationsEnabled -> user.smsNotificationsEnabled
        }

        dbUsers.document(fbUser.uid).update(property.name, newValue)
            .addOnFailureListener {
                Timber.e("Error syncing user property!")
            }
    }
}

@Suppress("SuspiciousVarProperty")
data class User(
    var name: String = "",
    var phone: String = "",
    var addresses: MutableList<Address> = mutableListOf(),
    var currentOrder: Order? = null,
    var pastOrders: MutableList<Order> = mutableListOf(),
    var pushNotificationsEnabled: Boolean = true,
    var smsNotificationsEnabled: Boolean = true,
    var isGuest: Boolean = false
) {
    var defaultAddress: Address = Address()
        get() {
            if (addresses.isEmpty())
                throw RuntimeException("Tried to get default address with no addresses set")


            val defaultAddressIndex = addresses.indexOfFirst { it.isDefault }

            return if (defaultAddressIndex != -1)
                addresses[defaultAddressIndex]
            else
                addresses.first()


        }

    var selectedAddress: Address = Address()
        get() {
            if (addresses.isEmpty())
                throw RuntimeException("Tried to get default address with no addresses set")

            val selectedAddressIndex = addresses.indexOfFirst { it.isSelected }

            return if (selectedAddressIndex != -1)
                addresses[selectedAddressIndex]
            else
                defaultAddress
        }
}