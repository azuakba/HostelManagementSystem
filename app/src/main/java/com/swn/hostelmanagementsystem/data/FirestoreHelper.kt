package com.swn.hostelmanagementsystem.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Add a new user to Firestore
    fun addUserToFirestore(name: String, email: String, role: String = "student", isApproved: Boolean=false ) {
        val userId = auth.currentUser?.uid ?: return
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "isApproved" to isApproved
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
}
