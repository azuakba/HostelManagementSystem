package com.swn.hostelmanagementsystem.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.student.Student
import com.swn.hostelmanagementsystem.ui.student.StudentAdapter
class ManageStudents : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var approvedAdapter: StudentAdapter
    private lateinit var unapprovedAdapter: UnapprovedStudentAdapter

    private lateinit var spinnerHostels: Spinner
    private lateinit var fabAddStudent: FloatingActionButton

    private val approvedStudentList = mutableListOf<Student>()
    private val unapprovedStudentList = mutableListOf<Student>()

    private val hostelList = mutableListOf<String>()
    private var selectedHostelId: String? = null
    private var showingUnapproved = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_dashboard_manage_students)

        recyclerView = findViewById(R.id.recyclerViewStudents)
        recyclerView.layoutManager= LinearLayoutManager(this)
        spinnerHostels = findViewById(R.id.spinnerHostels)
        fabAddStudent = findViewById(R.id.fabAddStudent)

        approvedAdapter = StudentAdapter(
            approvedStudentList,
            onAssignRoomClick = { student -> showAssignRoomDialog(student) },
            onEditClick = { student -> editStudent(student) },
            onDeleteClick = { student -> deleteStudent(student) }
        )

        unapprovedAdapter = UnapprovedStudentAdapter(
            unapprovedStudentList,
            onApproveClick = { student -> showHostelRoomDialogForApproval(student) },
            onRejectClick = { student -> rejectStudent(student) }
        )

// Set default adapter to approvedAdapter
        recyclerView.adapter = approvedAdapter


        loadHostels()

        fabAddStudent.setOnClickListener {
            if (selectedHostelId != null) {
                if (showingUnapproved) {
                    fetchApprovedStudents()
                    fabAddStudent.setImageResource(R.drawable.ic_unapproved)
                } else {
                    fetchUnapprovedStudents()
                    fabAddStudent.setImageResource(R.drawable.ic_approved)
                }
            } else {
                Toast.makeText(this, "Select a hostel first", Toast.LENGTH_SHORT).show()
            }
        }



        spinnerHostels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedHostelId = hostelList[position]
                fetchApprovedStudents()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun showHostelRoomDialogForApproval(student: Student) {
        if (selectedHostelId == null) {
            Toast.makeText(this, "Select a hostel first", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AssignRoomDialogFragment(student.id)
        dialog.onRoomAssigned = {
            // Approve the student and assign hostelId
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(student.id)
                .update(
                    mapOf(
                        "isApproved" to true,
                        "hostelId" to selectedHostelId
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "${student.name} approved", Toast.LENGTH_SHORT).show()
                    fetchUnapprovedStudents() // Refresh list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to approve student", Toast.LENGTH_SHORT).show()
                }
        }
        dialog.show(supportFragmentManager, "DialogApproveStudent")
    }
    private fun rejectStudent(student: Student) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(student.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${student.name} rejected and removed", Toast.LENGTH_SHORT).show()
                fetchUnapprovedStudents() // Refresh list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reject student", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editStudent(student: Student) {
        val dialog = AssignRoomDialogFragment(student.id, isEditMode = true)
        dialog.onRoomAssigned = {
            Toast.makeText(this, "${student.name}'s room updated", Toast.LENGTH_SHORT).show()
            fetchApprovedStudents()
        }
        dialog.show(supportFragmentManager, "EditStudentRoom")
    }



    private fun deleteStudent(student: Student) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(student.id)
            .update(
                mapOf(
                    "isApproved" to false,
                    "hostelId" to null,
                    "roomNumber" to null
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "${student.name} is now unapproved", Toast.LENGTH_SHORT).show()
                fetchApprovedStudents()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to unassign student", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadHostels() {
        FirebaseFirestore.getInstance().collection("hostels").get()
            .addOnSuccessListener { snapshot ->
                hostelList.clear()
                val hostelNames = mutableListOf<String>()
                for (doc in snapshot.documents) {
                    val hostelId = doc.id
                    val hostelName = doc.getString("name") ?: "Unnamed Hostel"
                    hostelList.add(hostelId)
                    hostelNames.add(hostelName)
                }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    hostelNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerHostels.adapter = adapter

                if (hostelList.isNotEmpty()) {
                    selectedHostelId = hostelList[0]
                    fetchApprovedStudents()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load hostels", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchApprovedStudents() {
        showingUnapproved = false
        if (selectedHostelId == null) return

        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "student")
            .whereEqualTo("isApproved", true)
            .whereEqualTo("hostelId", selectedHostelId)
            .get()
            .addOnSuccessListener { documents ->
                approvedStudentList.clear()
                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "N/A"
                    val email = doc.getString("email") ?: "N/A"
                    val room = doc.getString("roomNumber")
                    approvedStudentList.add(Student(id, name, email, true, room))
                }
                recyclerView.adapter = approvedAdapter
                approvedAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchUnapprovedStudents() {
        showingUnapproved = true
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { documents ->
                unapprovedStudentList.clear()
                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "N/A"
                    val email = doc.getString("email") ?: "N/A"
                    val isApproved = doc.getBoolean("isApproved") ?: false
                    val hostelId = doc.getString("hostelId")
                    val room = doc.getString("roomNumber")

                    if (!isApproved || hostelId.isNullOrEmpty()) {
                        unapprovedStudentList.add(Student(id, name, email, isApproved, room))
                    }
                }
                recyclerView.adapter = unapprovedAdapter
                unapprovedAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Showing unapproved/unassigned students", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showAssignRoomDialog(student: Student) {
        if (selectedHostelId == null) {
            Toast.makeText(this, "Select a hostel first", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = AssignRoomDialogFragment(student.id)
        dialog.onRoomAssigned = {
            Toast.makeText(this, "${student.name} approved", Toast.LENGTH_SHORT).show()
            fetchUnapprovedStudents() // Refresh list
        }
        dialog.show(supportFragmentManager, "DialogApproveStudent")

    }
}
