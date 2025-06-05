package com.swn.hostelmanagementsystem.model

data class Hostel(
    var hostelId: String = "",
    var name: String = "",
    var location: String = "",
    var totalCapacity: Int = 0,
    var totalOccupied: Int = 0
)

