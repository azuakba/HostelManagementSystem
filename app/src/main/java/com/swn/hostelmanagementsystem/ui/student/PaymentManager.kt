package com.swn.hostelmanagementsystem.ui.student

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

object PaymentManager {
    private val db = FirebaseFirestore.getInstance()

    fun addPayment(
        studentId: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val paymentId = db.collection("payments").document().id  // Generate unique payment ID

        val paymentData = hashMapOf(
            "id" to paymentId,
            "studentId" to studentId,
            "amount" to amount,
            "status" to "pending",       // Initial status before payment confirmation
            "timestamp" to Timestamp.now()
        )

        db.collection("payments").document(paymentId)
            .set(paymentData)
            .addOnSuccessListener {
                println("✅ Payment added successfully to Firestore!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                println("❌ Error adding payment: ${e.message}")
                onFailure(e)
            }
    }
}
