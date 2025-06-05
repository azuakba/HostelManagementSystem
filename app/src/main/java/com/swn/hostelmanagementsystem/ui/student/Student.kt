package com.swn.hostelmanagementsystem.ui.student

data class Student(
    val id: String,
    val name: String,
    val email: String,
    val isApproved: Boolean,
    val roomNumber: String? = null
)
