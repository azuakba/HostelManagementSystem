package com.swn.hostelmanagementsystem.model

data class StudentFeeStatus(
    val studentId: String = "",
    val name: String = "",
    val paidFees: List<String> = emptyList(),     // Titles of paid fees
    val unpaidFees: List<String> = emptyList()    // Titles of unpaid fees
)

