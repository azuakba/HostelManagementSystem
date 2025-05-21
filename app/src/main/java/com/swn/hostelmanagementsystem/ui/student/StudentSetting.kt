package com.swn.hostelmanagementsystem.ui.student

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity

class StudentSetting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_setting)
        val logoutButton = findViewById<MaterialButton>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Clear session or shared preferences
            val preferences = getSharedPreferences("user_session", MODE_PRIVATE)
            val editor = preferences.edit()
            editor.clear().apply()

            // Redirect to Login Screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}