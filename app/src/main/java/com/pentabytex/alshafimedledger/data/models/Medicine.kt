package com.pentabytex.alshafimedledger.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicine(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val volume: String = "",
    val quantity: Int = 0,
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0, // Optional pre-filled price
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
): Parcelable
