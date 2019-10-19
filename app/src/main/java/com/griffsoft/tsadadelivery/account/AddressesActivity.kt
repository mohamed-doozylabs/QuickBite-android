package com.griffsoft.tsadadelivery.account

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.griffsoft.tsadadelivery.R
import com.griffsoft.tsadadelivery.TDActivity
import com.griffsoft.tsadadelivery.UserUtil
import com.griffsoft.tsadadelivery.get_started.AddNewAddressSearchActivity
import com.griffsoft.tsadadelivery.objects.Address
import kotlinx.android.synthetic.main.activity_addresses.*
import kotlinx.android.synthetic.main.address_list_item.view.*
import kotlinx.android.synthetic.main.content_addresses.*

class AddressesActivity : TDActivity(), OnAddressDeletedListener {

    private lateinit var addressesAdapter: AddressesListAdapter
    private var selectedAddressPosition: Int = -1
    private lateinit var addresses: MutableList<Address>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addresses)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, AddNewAddressSearchActivity::class.java))
        }

        addresses = UserUtil.getCurrentUser(this)!!.addresses

        selectedAddressPosition = addresses.indexOfFirst { it.isSelected }

        addressesAdapter = AddressesListAdapter(this, addresses, this, true)
        addressesListView.adapter = addressesAdapter
        addressesListView.setOnItemClickListener { _, _, i, _ ->
            selectAddress(i)
        }
    }

    private fun selectAddress(position: Int) {
        val address = addresses[position]
        if (!address.isSelected) {
            UserUtil.setSelectedAddress(this, address.id)
        }
        selectedAddressPosition = position
        addressesAdapter.notifyDataSetChanged()
    }

    override fun deleteAddressTapped(position: Int) {
        val addressToBeDeleted = addresses[position]
        if (addressToBeDeleted.isSelected) {
            selectAddress(0)
        } else {
            selectedAddressPosition -= 1
        }
        UserUtil.removeAddress(this, addresses[position].id)
        addresses.removeAt(position)
        addressesAdapter.notifyDataSetChanged()

        Snackbar.make(coordinatorLayout, "Address deleted", Snackbar.LENGTH_SHORT).show()
    }

    inner class AddressesListAdapter(context: Context,
                                     private val addresses: MutableList<Address>,
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
                holder.deleteOrCheck.setImageResource(R.drawable.ic_check)
                holder.deleteOrCheck.visibility = if (address.isSelected) View.VISIBLE else View.INVISIBLE
            }

            holder.addressName.text = address.displayName

            if (address.floorDoorUnitNo.isNotEmpty()) {
                if (address.userNickname.isNotEmpty()) {
                    // First line is set to user nickname, so append street
                    holder.unitAndStreet.text = address.floorDoorUnitNo + ", " + address.street
                } else {
                    holder.unitAndStreet.text = address.floorDoorUnitNo
                }
            } else if (address.userNickname.isEmpty()) {
                // No unit or floor set, but there is a userNickname
                holder.unitAndStreet.text = address.street
            } else {
                holder.unitAndStreet.visibility = View.GONE
            }

            if (address.buildingLandmark.isNotEmpty()) {
                holder.buildingLandmark.text = address.buildingLandmark
            } else {
                holder.buildingLandmark.visibility = View.GONE
            }

            if (address.instructions.isNotBlank()) {
                holder.instructions.text  = "Instructions: " + address.instructions
            } else {
                holder.instructions.visibility = View.GONE
            }

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
