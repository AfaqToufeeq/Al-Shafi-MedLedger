package com.pentabytex.alshafimedledger.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    var userId: String = "",                 // Firebase Auth UID
    val name: String = "",                // Full name of the user
    val email: String = "",
    val password: String = "",
    val phone: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) : Parcelable
