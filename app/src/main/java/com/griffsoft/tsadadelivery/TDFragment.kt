package com.griffsoft.tsadadelivery


import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

abstract class TDFragment : Fragment() {

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//    }

    fun setupBackButton(rootView: View) {
        rootView.findViewById<ImageView?>(R.id.backButton)?.let {
            it.setOnClickListener { activity?.onBackPressed() }
        }
    }


    fun performSegue(resId: Int, bundle: Bundle? = null) {
        val options = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }

        findNavController().navigate(resId, bundle, options)
    }

}
