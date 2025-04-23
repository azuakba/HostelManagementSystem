package com.swn.hostelmanagementsystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity
import com.swn.hostelmanagementsystem.ui.auth.RegisterActivity
import com.swn.hostelmanagementsystem.ui.admin.AdminDashboardActivity
import com.swn.hostelmanagementsystem.ui.student.StudentDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // 🔐 User is already logged in
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role")
                    if (role == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, StudentDashboardActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Error fetching role, go to login as fallback
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        } else {
            // 🚪 No user logged in, show register/login options
            val registerButton: Button = findViewById(R.id.btnRegister)
            val loginButton: Button = findViewById(R.id.btnLogin)

            registerButton.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            loginButton.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
