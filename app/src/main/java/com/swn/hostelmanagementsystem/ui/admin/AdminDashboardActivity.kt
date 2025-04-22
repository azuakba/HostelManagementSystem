package com.swn.hostelmanagementsystem.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.navigation_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set the toolbar as the app bar
        setSupportActionBar(toolbar)

        // Enable the hamburger icon and sync with drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔥 Set admin name & email in header
        val headerView = navigationView.getHeaderView(0)
        val headerName = headerView.findViewById<TextView>(R.id.header_admin_name)
        val headerEmail = headerView.findViewById<TextView>(R.id.header_admin_email)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        // Set email
        headerEmail.text = currentUser?.email

        // Fetch name from Firestore
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        headerName.text = name ?: "Admin"
                    }
                }
                .addOnFailureListener {
                    headerName.text = "Admin"
                }
        }

        // Handle side nav clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, AdminDashboardProfileActivity::class.java)
                    startActivity(intent)
                    // Handle profile
                }
                R.id.nav_settings -> {
                    // Handle settings
                }
                // Add more cases as needed
            }
            drawerLayout.closeDrawers()
            true
        }

        // Handle bottom nav clicks
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    // Handle home
                    true
                }
                R.id.bottom_settings -> {
                    // Handle settings
                    true
                }
                else -> false
            }
        }
    }
}
