package com.swn.hostelmanagementsystem.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.auth.ChangePasswordActivity
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity
import com.swn.hostelmanagementsystem.ui.shared.EditProfile

class AdminSettings : AppCompatActivity() {

    private lateinit var tvEditProfile: TextView
    private lateinit var tvChangePassword: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings) // You can rename this XML later to `activity_admin_settings.xml`

        tvEditProfile = findViewById(R.id.tvEditProfile)
        tvChangePassword = findViewById(R.id.tvChangePassword)
        btnLogout = findViewById(R.id.btnLogout)

        tvEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        tvChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
