package com.swn.hostelmanagementsystem.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R

class AttendanceAdapter(
    private val students: List<AttendanceStudent>
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.tvStudentName)
        val emailText: TextView = itemView.findViewById(R.id.tvStudentEmail)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxPresent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        holder.nameText.text = student.name
        holder.emailText.text = student.email

        holder.checkBox.setOnCheckedChangeListener(null) // avoid recycling issues
        holder.checkBox.isChecked = student.isSelected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            student.isSelected = isChecked
        }
    }

    override fun getItemCount(): Int = students.size
}
