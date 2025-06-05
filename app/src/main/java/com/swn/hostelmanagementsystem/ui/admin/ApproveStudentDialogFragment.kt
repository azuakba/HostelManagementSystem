package com.swn.hostelmanagementsystem.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

class ApproveStudentDialogFragment(
    private val studentId: String,
    private val onStudentApproved: () -> Unit
) : DialogFragment() {

    private lateinit var spinnerHostels: Spinner
    private lateinit var spinnerRooms: Spinner
    private val firestore = FirebaseFirestore.getInstance()

    private val hostelIds = mutableListOf<String>()
    private val hostelNames = mutableListOf<String>()
    private val roomNumbers = mutableListOf<String>()

    private var selectedHostelId: String? = null
    private var selectedRoomNumber: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_approve_student, null)

        spinnerHostels = view.findViewById(R.id.spinnerHostels)
        spinnerRooms = view.findViewById(R.id.spinnerRooms)

        loadHostels()

        spinnerHostels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                selectedHostelId = hostelIds[position]
                loadRooms(selectedHostelId!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) {
                selectedRoomNumber = roomNumbers[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Approve Student")
            .setView(view)
            .setPositiveButton("Approve") { _, _ ->
                if (selectedHostelId != null && selectedRoomNumber != null) {
                    approveStudent()
                } else {
                    Toast.makeText(requireContext(), "Select hostel and room", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun loadHostels() {
        firestore.collection("hostels").get()
            .addOnSuccessListener { snapshot ->
                hostelIds.clear()
                hostelNames.clear()
                for (doc in snapshot.documents) {
                    hostelIds.add(doc.id)
                    hostelNames.add(doc.getString("name") ?: "Unnamed Hostel")
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hostelNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerHostels.adapter = adapter
                if (hostelIds.isNotEmpty()) {
                    selectedHostelId = hostelIds[0]
                    loadRooms(selectedHostelId!!)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load hostels", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRooms(hostelId: String) {
        firestore.collection("rooms")
            .whereEqualTo("hostelId", hostelId)
            .whereLessThan("occupied", "capacity") // or use a query to filter rooms with available capacity
            .get()
            .addOnSuccessListener { snapshot ->
                roomNumbers.clear()
                for (doc in snapshot.documents) {
                    val roomNum = doc.getString("roomNumber")
                    if (roomNum != null) roomNumbers.add(roomNum)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roomNumbers)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRooms.adapter = adapter
                if (roomNumbers.isNotEmpty()) {
                    selectedRoomNumber = roomNumbers[0]
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveStudent() {
        val updates = mapOf(
            "isApproved" to true,
            "hostelId" to selectedHostelId,
            "roomNumber" to selectedRoomNumber
        )
        firestore.collection("users").document(studentId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Student approved successfully", Toast.LENGTH_SHORT).show()
                onStudentApproved()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to approve student", Toast.LENGTH_SHORT).show()
            }
    }
}
