package com.swn.hostelmanagementsystem.ui.student

data class RequestItem(
    val studentName: String = "",
    val studentId: String = "",
    val type: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)
