package com.swn.hostelmanagementsystem.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.student.Student
import com.swn.hostelmanagementsystem.ui.student.StudentAdapter

class ManageStudents : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter
    private val studentList = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_dashboard_manage_students)

        recyclerView = findViewById(R.id.recyclerView_students)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList)
        recyclerView.adapter = adapter

        val addButton = findViewById<MaterialButton>(R.id.button_add_edit_students)
        addButton.setOnClickListener {
            startActivity(Intent(this, AddEditStudentActivity::class.java))
        }

        fetchStudents()
    }

    private fun fetchStudents() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { documents ->
                studentList.clear()
                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "N/A"
                    val email = doc.getString("email") ?: "N/A"
                    val isApproved = doc.getBoolean("isApproved") ?: false
                    studentList.add(Student(id, name, email, isApproved))
                }
                adapter.notifyDataSetChanged()
            }
    }
}
