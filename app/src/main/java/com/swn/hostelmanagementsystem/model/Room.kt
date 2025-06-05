package com.swn.hostelmanagementsystem.model
data class Room(
    val id: String = "",
    val roomNumber: String = "",
    val capacity: Int = 0,
    var occupied: Int = 0,
    val hostelId: String = ""
)

