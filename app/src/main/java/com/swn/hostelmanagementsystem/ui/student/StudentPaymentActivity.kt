package com.swn.hostelmanagementsystem.ui.student


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import org.json.JSONObject
import java.util.*

class StudentPaymentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var feeId: String
    private lateinit var feeType: String
    private var amount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_payment)
        supportActionBar?.title = "Processing Payment..."

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get data from intent
        feeId = intent.getStringExtra("feeId") ?: ""
        feeType = intent.getStringExtra("feeType") ?: ""
        amount = intent.getDoubleExtra("amount", 0.0)

        startRazorpayCheckout()
    }

    private fun startRazorpayCheckout() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_YQ75o5Z0wYMD2u") // Replace with your Razorpay key

        val obj = JSONObject()
        try {
            obj.put("name", "Hostel Management")
            obj.put("description", feeType)
            obj.put("currency", "INR")
            obj.put("amount", (amount * 100).toInt()) // Convert to paisa
            obj.put("prefill", JSONObject().apply {
                put("email", auth.currentUser?.email)
            })

            checkout.open(this, obj)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        val studentId = auth.currentUser?.uid ?: return

        // Payment document
        val payment = hashMapOf(
            "paymentId" to razorpayPaymentId,
            "studentId" to studentId,
            "feeId" to feeId,
            "feeType" to feeType,
            "amount" to amount,
            "status" to "paid",
            "timestamp" to Date()
        )

        firestore.collection("payments")
            .add(payment)
            .addOnSuccessListener {
                // Update or create student_fees doc for this student and fee
                val studentFeeDocId = "$studentId-$feeId" // unique doc id

                val studentFeeData = hashMapOf(
                    "studentId" to studentId,
                    "feeId" to feeId,
                    "feeType" to feeType,
                    "amount" to amount,
                    "status" to "paid",
                    "paymentId" to razorpayPaymentId,
                    "timestamp" to Date()
                )

                firestore.collection("student_fees").document(studentFeeDocId)
                    .set(studentFeeData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Payment saved but failed to update student fees", Toast.LENGTH_LONG).show()
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Payment Failed to Save", Toast.LENGTH_LONG).show()
                finish()
            }
    }


    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment Failed: $description", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 500)
    }
}
