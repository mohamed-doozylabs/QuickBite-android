package com.griffsoft.tsadadelivery.ui.account


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDFragment
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.extras.TDUtil
import com.griffsoft.tsadadelivery.get_started.AddNewAddressSearchActivity
import com.griffsoft.tsadadelivery.objects.Address
import kotlinx.android.synthetic.main.address_list_item.view.*

/**
 * A simple [Fragment] subclass.
 */
class AddressesFragment : TDFragment(), OnAddressDeletedListener {
    private lateinit var coordinatorLayout: CoordinatorLayout


    private lateinit var addressesAdapter: AddressesListAdapter
    private var selectedAddressPosition: Int = -1
    private lateinit var addresses: MutableList<Address>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_addresses, container, false)
        setupBackButton(root)
        coordinatorLayout = root.findViewById(R.id.coordinatorLayout)
        val addressesListView: ListView = root.findViewById(R.id.addressesListView)

        val settingsMode = arguments?.getBoolean("settingsMode") ?: false

        root.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(context!!, AddNewAddressSearchActivity::class.java))
        }

        addresses = UserUtil.getCurrentUser(context!!)!!.addresses

        selectedAddressPosition = addresses.indexOfFirst { it.selected }

        addressesAdapter = AddressesListAdapter(context!!, addresses, this, settingsMode)
        addressesListView.adapter = addressesAdapter
        addressesListView.setOnItemClickListener { _, _, i, _ ->
            selectAddress(i)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        val newAddresses = UserUtil.getCurrentUser(context!!)!!.addresses
        if (addresses != newAddresses) {
            // clear() and then add all so that the addresses object doesn't change
            addresses.clear()
            addresses.addAll(newAddresses)
            selectedAddressPosition = addresses.indexOfFirst { it.selected }
            addressesAdapter.notifyDataSetChanged()
        }
    }

    private fun selectAddress(position: Int) {
        val address = addresses[position]
        if (!address.selected) {
            UserUtil.setSelectedAddress(context!!, address.id)
        }
        selectedAddressPosition = position
        addressesAdapter.notifyDataSetChanged()
    }

    override fun deleteAddressTapped(position: Int) {
        val addressToBeDeleted = addresses[position]
        if (addressToBeDeleted.selected) {
            selectAddress(0)
        } else {
            selectedAddressPosition -= 1
        }
        UserUtil.removeAddress(context!!, addresses[position].id)
        addresses.removeAt(position)
        addressesAdapter.notifyDataSetChanged()

        Snackbar.make(coordinatorLayout, "Address removed", Snackbar.LENGTH_SHORT).show()
    }

    inner class AddressesListAdapter(context: Context,
                                     var addresses: MutableList<Address>,
                                     private val onDeleteListener: OnAddressDeletedListener,
                                     private val settingsMode: Boolean) :
        ArrayAdapter<Address>(context, R.layout.address_list_item, addresses) {


        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var rowView = convertView
            val holder: AddressListViewHolder

            if (rowView == null) {
                rowView = LayoutInflater.from(context).inflate(R.layout.address_list_item, parent, false)
                holder = AddressListViewHolder(rowView)
                rowView.tag = holder
            } else {
                holder = rowView.tag as AddressListViewHolder
            }

            val address = getItem(position)!!
            if (settingsMode) {
                holder.currentlySelectedLabel.visibility = if (position == selectedAddressPosition) View.VISIBLE else View.INVISIBLE
                // Hide delete button if there is only one address
                if (addresses.size == 1) {
                    holder.deleteOrCheck.visibility = View.INVISIBLE
                } else {
                    holder.deleteOrCheck.visibility = View.VISIBLE
                    holder.deleteOrCheck.setOnClickListener {
                        onDeleteListener.deleteAddressTapped(position)
                    }
                }
            } else {
                holder.currentlySelectedLabel.visibility = View.INVISIBLE
                holder.deleteOrCheck.setImageResource(R.drawable.ic_check)
                if (position == selectedAddressPosition) {
                    holder.deleteOrCheck.visibility = View.VISIBLE
                    holder.addressName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                } else {
                    holder.deleteOrCheck.visibility = View.INVISIBLE
                    holder.addressName.setTextColor(Color.BLACK)
                }
            }

            TDUtil.populateAddressFields(
                address,
                holder.addressName,
                holder.unitAndStreet,
                holder.buildingLandmark,
                holder.instructions
            )

            return rowView!!
        }

        inner class AddressListViewHolder(view: View){
            val currentlySelectedLabel = view.currentlySelectedLabel as TextView
            val addressName = view.addressName as TextView
            val unitAndStreet = view.unitAndStreet as TextView
            val buildingLandmark = view.buildingLandmark as TextView
            val instructions = view.instructions as TextView
            val deleteOrCheck = view.deleteOrCheck as ImageView
        }
    }
}

interface OnAddressDeletedListener {
    fun deleteAddressTapped(position: Int)
}
