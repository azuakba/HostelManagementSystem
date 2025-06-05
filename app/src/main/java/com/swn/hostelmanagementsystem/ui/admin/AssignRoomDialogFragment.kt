package com.swn.hostelmanagementsystem.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class AssignRoomDialogFragment(
    private val studentId: String,
    private val isEditMode: Boolean = false  // new flag
) : DialogFragment() {

    var onRoomAssigned: (() -> Unit)? = null

    private val db = FirebaseFirestore.getInstance()
    private val hostelMap = mutableMapOf<String, String>() // hostelName -> hostelId
    private val roomList = mutableListOf<String>()

    private lateinit var spinnerHostels: Spinner
    private lateinit var spinnerRooms: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        spinnerHostels = Spinner(context)
        spinnerRooms = Spinner(context)

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 10)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            addView(TextView(context).apply { text = "Select Hostel:" })
            addView(spinnerHostels)

            addView(TextView(context).apply { text = "Select Room:" })
            addView(spinnerRooms)
        }

        val builder = AlertDialog.Builder(context)
            .setTitle(if (isEditMode) "Edit Hostel and Room" else "Assign Hostel and Room")
            .setView(layout)
            .setPositiveButton(if (isEditMode) "Update" else "Assign", null)
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()

        dialog.setOnShowListener {
            val assignButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            assignButton.setOnClickListener {
                val selectedHostelName = spinnerHostels.selectedItem as? String
                val selectedRoom = spinnerRooms.selectedItem as? String

                val selectedHostelId = hostelMap[selectedHostelName]
                if (selectedHostelId == null || selectedRoom.isNullOrEmpty()) {
                    Toast.makeText(context, "Please select both hostel and room", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                assignRoomToStudent(selectedHostelId, selectedRoom, dialog)
            }
        }

        loadHostels()

        return dialog
    }

    private fun loadHostels() {
        db.collection("hostels").get()
            .addOnSuccessListener { snapshot ->
                val hostelNames = mutableListOf<String>()
                hostelMap.clear()

                for (doc in snapshot) {
                    val name = doc.getString("name") ?: "Unnamed Hostel"
                    hostelMap[name] = doc.id
                    hostelNames.add(name)
                }

                val hostelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, hostelNames)
                spinnerHostels.adapter = hostelAdapter

                spinnerHostels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        val selectedHostelName = hostelNames[position]
                        val hostelId = hostelMap[selectedHostelName] ?: return
                        loadAvailableRooms(hostelId)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                if (hostelNames.isNotEmpty()) {
                    spinnerHostels.setSelection(0)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load hostels", Toast.LENGTH_SHORT).show()
                dismiss()
            }
    }

    private fun loadAvailableRooms(hostelId: String) {
        db.collection("rooms")
            .whereEqualTo("hostelId", hostelId)
            .get()
            .addOnSuccessListener { roomSnapshot ->
                val availableRooms = mutableListOf<String>()
                val allRooms = roomSnapshot.documents

                if (allRooms.isEmpty()) {
                    Toast.makeText(requireContext(), "No rooms found in selected hostel", Toast.LENGTH_SHORT).show()
                    spinnerRooms.adapter = null
                    return@addOnSuccessListener
                }

                var processed = 0

                for (roomDoc in allRooms) {
                    val roomNumber = roomDoc.getString("roomNumber") ?: continue
                    val capacity = roomDoc.getLong("capacity") ?: 0L

                    // Count assigned students in this room
                    db.collection("users")
                        .whereEqualTo("roomNumber", roomNumber)
                        .whereEqualTo("hostelId", hostelId)
                        .get()
                        .addOnSuccessListener { studentsSnapshot ->
                            if (studentsSnapshot.size() < capacity) {
                                availableRooms.add(roomNumber)
                            }
                            processed++
                            if (processed == allRooms.size) {
                                updateRoomSpinner(availableRooms)
                            }
                        }
                        .addOnFailureListener {
                            processed++
                            if (processed == allRooms.size) {
                                updateRoomSpinner(availableRooms)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRoomSpinner(availableRooms: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, availableRooms)
        spinnerRooms.adapter = adapter
    }

    private fun assignRoomToStudent(hostelId: String, roomNumber: String, dialog: Dialog) {
        val studentRef = db.collection("users").document(studentId)

        db.collection("rooms")
            .whereEqualTo("roomNumber", roomNumber)
            .whereEqualTo("hostelId", hostelId)
            .limit(1)
            .get()
            .addOnSuccessListener { roomSnapshot ->
                if (roomSnapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Room not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val roomDoc = roomSnapshot.documents[0]
                val capacity = roomDoc.getLong("capacity") ?: 0L

                db.collection("users")
                    .whereEqualTo("roomNumber", roomNumber)
                    .whereEqualTo("hostelId", hostelId)
                    .get()
                    .addOnSuccessListener { assignedStudents ->
                        if (assignedStudents.size() >= capacity) {
                            Toast.makeText(requireContext(), "Room is full", Toast.LENGTH_SHORT).show()
                        } else {
                            val updateMap = mutableMapOf<String, Any>(
                                "hostelId" to hostelId,
                                "roomNumber" to roomNumber
                            )
                            // Only set isApproved = true if not editing
                            if (!isEditMode) {
                                updateMap["isApproved"] = true
                            }

                            studentRef.update(updateMap)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), if (isEditMode) "Student room updated" else "Student approved", Toast.LENGTH_SHORT).show()
                                    onRoomAssigned?.invoke()
                                    dialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to update student", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch room info", Toast.LENGTH_SHORT).show()
            }
    }
}
