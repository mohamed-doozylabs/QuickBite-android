package com.griffsoft.tsadadelivery

import android.content.res.Resources

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


val Double.asPriceString: String
    get() {
        val string = this.toString()


        return if (string.substringAfter(".").contains("""[123456789]""".toRegex())) {
            "₱$string"
        } else {
            "₱${string.substringBefore(".")}"
        }
    }