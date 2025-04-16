package com.swn.hostelmanagementsystem.ui.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private val studentList = mutableListOf<Student>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        studentAdapter = StudentAdapter(studentList)
        recyclerView.adapter = studentAdapter

        fetchStudents()
    }

    private fun fetchStudents() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("role", "student")  // Fetch only students
            .get()
            .addOnSuccessListener { documents ->
                studentList.clear()
                for (document in documents) {
                    val student = document.toObject(Student::class.java).copy(id = document.id)
                    studentList.add(student)
                }
                studentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching students", e)
            }
    }


}
