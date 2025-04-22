package com.swn.hostelmanagementsystem.ui.student

import android.app.Activity
import android.widget.Toast
import com.razorpay.Checkout
import org.json.JSONObject

object PaymentHelper {

    fun startPayment(activity: Activity, amount: Double, studentId: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_yourapikey")  // 🔹 Replace with your Razorpay Test Key

        val paymentDetails = JSONObject()
        paymentDetails.put("name", "Hostel Management")
        paymentDetails.put("description", "Hostel Fees Payment")
        paymentDetails.put("currency", "INR")
        paymentDetails.put("amount", (amount * 100).toInt())  // Amount in paise
        paymentDetails.put("prefill", JSONObject().apply {
            put("email", "test@example.com")  // Replace with actual user email
            put("contact", "9876543210")      // Replace with actual phone number
        })

        try {
            checkout.open(activity, paymentDetails)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
