package com.pentabytex.alshafimedledger.enums

import androidx.annotation.ColorRes
import com.pentabytex.alshafimedledger.R

enum class PaymentStatus(val displayName: String, @ColorRes val colorRes: Int) {
    ALL("All", R.color.colorPrimary),
    PENDING("Pending", R.color.status_pending),
    RECEIVED("Received", R.color.status_completed),
    NOT_RECEIVED("Not Received", R.color.status_rejected);

    companion object {
        fun fromString(status: String): PaymentStatus? {
            return entries.find { it.displayName.equals(status, ignoreCase = true) }
        }
    }
}

