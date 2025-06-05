package com.swn.hostelmanagementsystem.ui.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import java.util.*

class AdminAttendanceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSaveAttendance: Button

    private val db = FirebaseFirestore.getInstance()
    private val allStudents = mutableListOf<AttendanceStudent>()
    private lateinit var adapter: AttendanceAdapter

    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_attendance)

        recyclerView = findViewById(R.id.rvStudents)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter(allStudents)
        recyclerView.adapter = adapter

        btnSelectDate.setOnClickListener { showDatePicker() }
        btnSaveAttendance.setOnClickListener { saveAttendance() }

        loadStudents()
    }

    private fun loadStudents() {
        db.collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { result ->
                allStudents.clear()
                for (doc in result) {
                    val userName = doc.getString("name") ?: ""
                    val userEmail = doc.getString("email") ?: ""
                    Log.d("AttendanceDebug", "Loaded student: $userName - $userEmail")
                    allStudents.add(AttendanceStudent(userName, userEmail, false))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val dpd = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            tvSelectedDate.text = "Date: $selectedDate"

            allStudents.forEach { it.isSelected = false }
            loadAttendanceForDate(selectedDate)

        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dpd.show()
    }

    private fun loadAttendanceForDate(date: String) {
        db.collection("attendance")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val email = doc.getString("userEmail")
                    val status = doc.getString("status")
                    allStudents.find { it.email == email }?.isSelected = (status == "present")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load attendance for $date", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAttendance() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val batch = db.batch()
        var tasksCompleted = 0

        allStudents.forEach { student ->
            val status = if (student.isSelected) "present" else "absent"

            db.collection("attendance")
                .whereEqualTo("userName",student.name)
                .whereEqualTo("userEmail", student.email)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val docRef = querySnapshot.documents[0].reference
                        batch.update(docRef, "status", status)
                    } else {
                        val newRecord = AttendanceRecord(
                            userName = student.name,
                            userEmail = student.email,
                            date = selectedDate,
                            status = status
                        )
                        val newDocRef = db.collection("attendance").document()
                        batch.set(newDocRef, newRecord)
                    }

                    tasksCompleted++
                    if (tasksCompleted == allStudents.size) {
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save attendance", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to check attendance for ${student.name}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
