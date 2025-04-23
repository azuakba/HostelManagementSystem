package com.swn.hostelmanagementsystem.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

class AddEditStudentActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var roomEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_student)

        nameEditText = findViewById(R.id.editText_student_name)
        emailEditText = findViewById(R.id.editText_student_email)
        roomEditText = findViewById(R.id.editText_student_room)
        saveButton = findViewById(R.id.button_save_student)

        saveButton.setOnClickListener {
            saveStudent()
        }
    }

    private fun saveStudent() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val room = roomEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || room.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val student = hashMapOf(
            "name" to name,
            "email" to email,
            "room" to room,
            "role" to "student",
            "isApproved" to false
        )

        db.collection("users").add(student).addOnSuccessListener {
            Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show()
            finish() // Go back to ManageStudents
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
        }
    }
}
