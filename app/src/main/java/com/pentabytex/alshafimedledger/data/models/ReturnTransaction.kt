package com.pentabytex.alshafimedledger.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReturnTransaction(
    val id: String = "",
    val saleId: String = "",
    val medicineId: String = "",
    val customerId: String = "",
    val quantityReturned: Int = 0,
    val returnValue: Double = 0.0,
    val notes: String = "No notes",
    val timestamp: Long = System.currentTimeMillis()
): Parcelable
