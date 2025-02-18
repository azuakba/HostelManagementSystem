package com.swn.hostelmanagementsystem.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Add a new user to Firestore
    fun addUserToFirestore(name: String, email: String, role: String = "student") {
        val userId = auth.currentUser?.uid ?: return
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role
        )
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                println("User successfully added to Firestore.")
            }
            .addOnFailureListener { e ->
                // Handle failure
                println("Error adding user to Firestore: ${e.message}")
            }
    }



//    // Example: Add a room
//    fun addRoom(hostelId: String, roomNumber: String, capacity: Int) {
//        val room = hashMapOf(
//            "hostelId" to db.collection("hostels").document(hostelId),
//            "roomNumber" to roomNumber,
//            "capacity" to capacity
//        )
//        db.collection("rooms").add(room)
//            .addOnSuccessListener {
//                // Handle success
//            }
//            .addOnFailureListener {
//                // Handle failure
//            }
//    }
}
