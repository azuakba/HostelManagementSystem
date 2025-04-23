package com.swn.hostelmanagementsystem.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.swn.hostelmanagementsystem.R

class StudentDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        // Find all TextView elements by ID
        val profile = findViewById<TextView>(R.id.profile)
        val roomDetails = findViewById<TextView>(R.id.roomDetails)
        val payment = findViewById<TextView>(R.id.payment)
        val attendance = findViewById<TextView>(R.id.attendance)
        val complainsAndRequests = findViewById<TextView>(R.id.complainsAndRequests)
        val settings = findViewById<TextView>(R.id.settings)

        // Set up click listeners
        profile.setOnClickListener {
            startActivity(Intent(this, StudentDashboardProfile::class.java))
        }

        roomDetails.setOnClickListener {
            startActivity(Intent(this, StudentDashboardRoomDetails::class.java))
        }

        payment.setOnClickListener {
            startActivity(Intent(this, StudentDashboardPayment::class.java))
        }

        attendance.setOnClickListener {
            startActivity(Intent(this, StudentDashboardAttendance::class.java))
        }

        complainsAndRequests.setOnClickListener {
            startActivity(Intent(this, StudentDashboardComplainAndRequest::class.java))
        }

        settings.setOnClickListener {
            startActivity(Intent(this, StudentSetting::class.java))
        }
    }
}
