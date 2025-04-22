package com.swn.hostelmanagementsystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.FirebaseApp
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity
import com.swn.hostelmanagementsystem.ui.auth.RegisterActivity
import com.swn.hostelmanagementsystem.ui.student.PaymentHelper

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        PaymentHelper.startPayment(this, 5000.0, "exampleStudentID")

        val registerButton: Button = findViewById(R.id.btnRegister)
        val loginButton: Button = findViewById(R.id.btnLogin)

        registerButton.setOnClickListener {
            navigateToRegister()
        }

        loginButton.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToRegister() {

        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}
