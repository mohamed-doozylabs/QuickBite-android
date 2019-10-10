package com.griffsoft.tsadadelivery

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object OrderUtil {
    fun formatDate(timestamp: Timestamp): String {
        val mins = (Date().time - timestamp.toDate().time) / 60000

        if (mins > 59) {
            val hours = mins / 60
            if (hours > 23) {
                val days = hours / 24
                return "$days day${if (days > 1) "s" else ""} ago"
            }
            return "$hours hour${if (hours > 1) "s" else ""} ago"
        } else if (mins < 1) {
            return "Just now"
        }

        return "$mins min${if (mins > 1) "s" else ""} ago"
    }

    fun getDate(timestamp: Timestamp): String {
        val formatter = SimpleDateFormat("EEE, MMM d, hh:mm aaa", Locale.ENGLISH)
        return formatter.format(timestamp.toDate())
    }

    fun interpretOrderStage(stage: Int): String {
        return when(stage) {
            0 -> "Submitted By Customer"
            1 -> "Being Prepared By Store"
            2 -> "En Route to Customer"
            3 -> "Delivered"
            else -> ""
        }
    }
}