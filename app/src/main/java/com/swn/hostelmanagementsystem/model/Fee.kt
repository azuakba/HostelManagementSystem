package com.swn.hostelmanagementsystem.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fee(
    var id: String? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val frequency: Int = 1,
    val type: String = "",
    val description: String = "",
    val hostelId: String? = null,
    val studentId: String? = null,
    var status: String = "unpaid"
) : Parcelable



