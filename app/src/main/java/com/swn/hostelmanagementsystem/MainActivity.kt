package com.swn.hostelmanagementsystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity
import com.swn.hostelmanagementsystem.ui.auth.RegisterActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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