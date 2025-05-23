package com.pentabytex.alshafimedledger.data.models

import android.os.Parcelable
import com.pentabytex.alshafimedledger.enums.PaymentStatus
import kotlinx.parcelize.Parcelize


@Parcelize
data class Sale(
    val saleId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val saleItems: List<SaleItem> = emptyList(),
    val notes: String = "No notes",
    val totalPrice: Double = 0.0,
    val amountReceived: Double = 0.0,             // NEW
    val paymentStatus: String = PaymentStatus.PENDING.displayName,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SaleItem(
    val medicineId: String = "",
    val medicineName: String = "",
    val quantitySold: Int = 0,
    val purchasePricePerUnit: Double = 0.0,
    val sellingPricePerUnit: Double = 0.0,
    val totalSellingPrice: Double = 0.0,
    val totalPurchaseCost: Double = 0.0
): Parcelable
