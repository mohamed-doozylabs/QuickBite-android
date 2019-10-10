package com.griffsoft.tsadadelivery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_order_status_dialog.*

private const val ARG_CURRENT_STATUS = "currentStatus"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OrderStatusDialogFragment.OrderStatusDialogDelegate] interface
 * to handle interaction events.
 * Use the [OrderStatusDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderStatusDialogFragment : DialogFragment(), View.OnClickListener {
    private var currentStatus: Int? = null
    private var delegate: OrderStatusDialogDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentStatus = it.getInt(ARG_CURRENT_STATUS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_status_dialog, container, false)
        this.dialog!!.setTitle(R.string.change_order_status)

        view.findViewById<Button>(R.id.saveButton).setOnClickListener(this)
        view.findViewById<Button>(R.id.cancelButton).setOnClickListener(this)

        val currentStatusRadioGroup = view.findViewById<RadioGroup>(R.id.orderStatusRadioGroup)
        currentStatusRadioGroup.check(getIdForCurrentStageRadioButton())

        return view

    }

    private fun getIdForCurrentStageRadioButton(): Int {
        return when(currentStatus!!) {
            0 -> R.id.orderSubmittedRadioButton
            1 -> R.id.beingPreparedRadioButton
            2 -> R.id.enRouteRadioButton
            3 -> R.id.deliveredRadioButton
            else -> 0
        }
    }

    private fun saveTapped() {
        val selectedStage = orderStatusRadioGroup.findViewById<View>(orderStatusRadioGroup.checkedRadioButtonId)
        delegate?.newOrderStatusSelected(orderStatusRadioGroup.indexOfChild(selectedStage))
        this.dismiss()
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.saveButton -> saveTapped()
            R.id.cancelButton -> this.dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OrderStatusDialogDelegate) {
            delegate = context
        } else {
            throw RuntimeException(context.toString() + " must implement OrderStatusDialogDelegate")
        }
    }

    override fun onDetach() {
        super.onDetach()
        delegate = null
    }

    interface OrderStatusDialogDelegate {
        fun newOrderStatusSelected(newStage: Int)
    }

    companion object {
        @JvmStatic
        fun newInstance(currentStatus: Int) =
            OrderStatusDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CURRENT_STATUS, currentStatus)
                }
            }
    }
}
