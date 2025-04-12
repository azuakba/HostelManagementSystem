package com.swn.hostelmanagementsystem.ui.student

data class  Student(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    var isApproved: Boolean = false  // This must be mutable so we can update it
)
