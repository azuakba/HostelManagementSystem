package com.swn.hostelmanagementsystem.ui.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.swn.hostelmanagementsystem.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64


class AdminDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var textAdminName: TextView
    private lateinit var textAdminEmail: TextView
    private lateinit var textTotalStudents: TextView
    private lateinit var textOccupiedRooms: TextView
    private lateinit var textFeesCollected: TextView
    private lateinit var textPendingPayments: TextView
    private lateinit var recentRequestsLayout: LinearLayout

    private lateinit var headerView: View
    private lateinit var headerName: TextView
    private lateinit var headerEmail: TextView
    private lateinit var headerProfileImage: ImageView


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()




        // Initialize UI views
        initializeViews()

        // Setup toolbar and navigation drawer toggle
        setupToolbarAndDrawer()

        headerView = navigationView.getHeaderView(0)
        headerName = headerView.findViewById(R.id.header_admin_name)
        headerEmail = headerView.findViewById(R.id.header_admin_email)
        headerProfileImage = headerView.findViewById(R.id.header_admin_photo)

        loadAdminHeaderInfo()

        // Set NavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener(this)



        // Setup SwipeRefreshLayout refresh action
        swipeRefreshLayout.setOnRefreshListener {
            loadDashboardData()
        }

        // Load dashboard data initially with refresh indicator
        swipeRefreshLayout.isRefreshing = true
        loadDashboardData()

        // Override back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Re-set listener on resume to avoid issues after returning from fragments
        navigationView.setNavigationItemSelectedListener(this)
        toggle.syncState()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)

        textAdminName = findViewById(R.id.text_admin_name)
        textAdminEmail = findViewById(R.id.text_admin_email)
        textTotalStudents = findViewById(R.id.text_total_students)
        textOccupiedRooms = findViewById(R.id.text_occupied_rooms)
        textFeesCollected = findViewById(R.id.text_fees_collected)
        textPendingPayments = findViewById(R.id.text_pending_payments)
        recentRequestsLayout = findViewById(R.id.recent_requests_layout)
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun loadDashboardData() {
        // Count tasks to manage refreshing state
        var tasksRemaining = 6

        fun onTaskComplete() {
            tasksRemaining--
            if (tasksRemaining <= 0) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        loadAdminInfo { onTaskComplete() }
        loadStudentCount { onTaskComplete() }
        loadOccupiedRooms { onTaskComplete() }
        loadFeesCollected { onTaskComplete() }
        loadPendingPayments { onTaskComplete() }
        loadRecentRequests { onTaskComplete() }
    }

    private fun loadAdminInfo(onComplete: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show()
            onComplete()
            return
        }

        textAdminEmail.text = user.email ?: "No Email"

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Admin"
                textAdminName.text = name
                onComplete()
            }
            .addOnFailureListener {
                textAdminName.text = "Admin"
                Toast.makeText(this, "Failed to load admin info", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private fun loadAdminHeaderInfo() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show()
            return
        }

        headerEmail.text = user.email ?: "No Email"

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Admin"
                headerName.text = name

                val base64Image = doc.getString("profileImage")
                if (!base64Image.isNullOrEmpty()) {
                    val bitmap = base64ToBitmap(base64Image)
                    if (bitmap != null) {
                        headerProfileImage.setImageBitmap(bitmap)
                    } else {
                        headerProfileImage.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                    headerProfileImage.setImageResource(R.drawable.ic_profile)
                }
            }
            .addOnFailureListener {
                headerName.text = "Admin"
                headerProfileImage.setImageResource(R.drawable.ic_profile)
                Toast.makeText(this, "Failed to load admin info", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadStudentCount(onComplete: () -> Unit) {
        db.collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { snapshot ->
                textTotalStudents.text = snapshot.size().toString()
                onComplete()
            }
            .addOnFailureListener {
                textTotalStudents.text = "-"
                Toast.makeText(this, "Failed to load student count", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun loadOccupiedRooms(onComplete: () -> Unit) {
        db.collection("rooms").get()
            .addOnSuccessListener { roomsSnapshot ->
                val rooms = roomsSnapshot.documents
                val totalRooms = rooms.size

                db.collection("users")
                    .whereEqualTo("role", "student")
                    .whereEqualTo("isApproved", true)
                    .get()
                    .addOnSuccessListener { studentsSnapshot ->

                        val roomOccupancyMap = mutableMapOf<String, Int>()

                        for (doc in studentsSnapshot) {
                            val roomNumber = doc.getString("roomNumber")
                            if (!roomNumber.isNullOrEmpty()) {
                                roomOccupancyMap[roomNumber] = roomOccupancyMap.getOrDefault(roomNumber, 0) + 1
                            }
                        }

                        var occupiedRoomsCount = 0
                        for (room in rooms) {
                            val roomNumber = room.getString("roomNumber")
                            val capacity = room.getLong("capacity")?.toInt() ?: 0
                            val occupied = roomOccupancyMap[roomNumber] ?: 0
                            if (occupied >= capacity) {
                                occupiedRoomsCount++
                            }
                        }

                        textOccupiedRooms.text = "$occupiedRoomsCount / $totalRooms"
                        onComplete()

                    }
                    .addOnFailureListener {
                        textOccupiedRooms.text = "- / -"
                        Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }
            }
            .addOnFailureListener {
                textOccupiedRooms.text = "- / -"
                Toast.makeText(this, "Failed to fetch rooms", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun loadFeesCollected(onComplete: () -> Unit) {
        db.collection("payments")
            .whereEqualTo("status", "success")
            .get()
            .addOnSuccessListener { snapshot ->
                val total = snapshot.sumOf { it.getDouble("amount") ?: 0.0 }
                textFeesCollected.text = "₹ %.2f".format(total)
                onComplete()
            }
            .addOnFailureListener {
                textFeesCollected.text = "-"
                Toast.makeText(this, "Failed to load fees collected", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun loadPendingPayments(onComplete: () -> Unit) {
        db.collection("payments")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val totalPending = snapshot.sumOf { it.getDouble("amount") ?: 0.0 }
                textPendingPayments.text = "₹ %.2f".format(totalPending)
                onComplete()
            }
            .addOnFailureListener {
                textPendingPayments.text = "-"
                Toast.makeText(this, "Failed to load pending payments", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun loadRecentRequests(onComplete: () -> Unit) {
        db.collection("requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                recentRequestsLayout.removeAllViews()

                val padding = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics
                ).toInt()

                for (doc in result) {
                    val description = doc.getString("description") ?: continue
                    val type = doc.getString("type") ?: "Request"
                    //val studentId = doc.getString("studentId") ?: "Unknown"
                    val studentName = doc.getString("studentName") ?: "Unknown"


                    val textView = TextView(this).apply {
                        text = "• [$type] $description (by $studentName)"
                        setPadding(padding, padding, padding, padding)
                    }
                    recentRequestsLayout.addView(textView)
                }
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load recent requests", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_hostel -> startActivity(Intent(this, HostelManagementActivity::class.java))
            R.id.nav_notices -> startActivity(Intent(this, AdminNoticeActivity::class.java))
            R.id.nav_attendance -> startActivity(Intent(this, AdminAttendanceActivity::class.java))
            R.id.nav_profile -> startActivity(Intent(this, AdminDashboardProfileActivity::class.java))
            R.id.nav_students -> startActivity(Intent(this, ManageStudents::class.java))
            R.id.nav_fees -> startActivity(Intent(this, FeeManagement::class.java))
            R.id.nav_rooms -> startActivity(Intent(this, RoomManagementActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, AdminSettings::class.java))
            else -> return false
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleBackPressed() {
        val container = findViewById<View>(R.id.admin_fragment_container)

        if (container.visibility == View.VISIBLE) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                findViewById<FrameLayout>(R.id.admin_fragment_container).visibility = View.GONE
                container.visibility = View.GONE
                container.isClickable = false
                container.isFocusable = false
                return
            }
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        // Otherwise exit the app/activity
        finish()
    }
}
