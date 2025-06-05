package com.swn.hostelmanagementsystem.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.swn.hostelmanagementsystem.R

class StudentAdapter(
    private val students: List<Student>,
    private val onAssignRoomClick: (Student) -> Unit,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.studentName)
        val emailTextView: TextView = itemView.findViewById(R.id.studentEmail)
        val roomTextView: TextView = itemView.findViewById(R.id.studentRoom)
        val assignRoomButton: MaterialButton = itemView.findViewById(R.id.assignRoomButton)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)

        // Optional dropdowns for hostel/room (only for unapproved)
        val hostelDropdown: Spinner? = itemView.findViewById(R.id.spinnerHostelAssign)
        val roomDropdown: Spinner? = itemView.findViewById(R.id.spinnerRoomAssign)
        val layoutDropdowns: LinearLayout? = itemView.findViewById(R.id.layoutDropdowns)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]

        holder.nameTextView.text = student.name
        holder.emailTextView.text = student.email

        if (!student.roomNumber.isNullOrBlank()) {
            holder.roomTextView.visibility = View.VISIBLE
            holder.roomTextView.text = "Room: ${student.roomNumber}"
        } else {
            holder.roomTextView.visibility = View.GONE
        }

        if (student.isApproved) {
            // Approved student view: Edit + Delete
            holder.assignRoomButton.text = "Edit"
            holder.assignRoomButton.setIconResource(R.drawable.ic_edit)
            holder.assignRoomButton.setOnClickListener {
                onEditClick(student)
            }

            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClick(student)
            }

            holder.layoutDropdowns?.visibility = View.GONE

        } else {
            // Unapproved student view: Assign Room + Dropdowns
            holder.assignRoomButton.text = "Assign Room"
            holder.assignRoomButton.setIconResource(R.drawable.ic_check)
            holder.assignRoomButton.setOnClickListener {
                onAssignRoomClick(student)
            }

            holder.deleteButton.visibility = View.GONE

            // TODO: You can later populate these dropdowns dynamically
            holder.layoutDropdowns?.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = students.size
}
