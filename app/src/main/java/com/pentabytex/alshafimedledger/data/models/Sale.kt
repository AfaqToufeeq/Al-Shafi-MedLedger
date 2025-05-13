package com.pentabytex.alshafimedledger.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sale(
    val id: String = "",
    val medicineId: String = "",
    val customerId: String = "",
    val quantitySold: Int = 0,
    val sellingPricePerUnit: Double = 0.0,
    val totalSellingPrice: Double = 0.0,
    val totalPurchaseCost: Double = 0.0,
    val profit: Double = 0.0,
    val profitPercentage: Double = 0.0,
    val notes: String = "No notes",
    val timestamp: Long = System.currentTimeMillis()
): Parcelable
