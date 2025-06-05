package com.swn.hostelmanagementsystem.ui.student

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.shared.SettingsFragment

class StudentMainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main)

        bottomNav = findViewById(R.id.student_bottom_nav)
        FirebaseMessaging.getInstance().subscribeToTopic("students")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to students topic")
                } else {
                    Log.e("FCM", "Subscription failed", task.exception)
                }
            }


        // Load default fragment
        loadFragment(StudentDashboardFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_student_home -> {
                    loadFragment(StudentDashboardFragment())
                    true
                }
                R.id.bottom_attendance -> {
                    loadFragment(StudentAttendanceFragment())
                    true
                }
                R.id.bottom_student_requests -> {
                    loadFragment(StudentDashboardComplainAndRequestFragment())
                    true
                }
                R.id.bottom_student_fees -> {
                    loadFragment(StudentDashboardPaymentFragment())
                    true
                }
                R.id.bottom_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_fragment_container, fragment)
            .commit()
    }
}
