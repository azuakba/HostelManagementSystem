package com.swn.hostelmanagementsystem

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.ui.admin.AdminDashboardActivity
import com.swn.hostelmanagementsystem.ui.auth.LoginActivity
import com.swn.hostelmanagementsystem.ui.student.StudentMainActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        lottieAnimationView = findViewById(R.id.lottie_animation)

        // Listen for animation end, then check user and navigate
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                checkUserAndNavigate()
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun checkUserAndNavigate() {
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
                        startActivity(Intent(this, StudentMainActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Error fetching role, fallback to login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        } else {
            // 🚪 No user logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
