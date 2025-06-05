package com.swn.hostelmanagementsystem.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.student.Student

class UnapprovedStudentAdapter(
    private val students: List<Student>,
    private val onApproveClick: (Student) -> Unit,
    private val onRejectClick: (Student) -> Unit
) : RecyclerView.Adapter<UnapprovedStudentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTv: TextView = itemView.findViewById(R.id.studentName)
        val emailTv: TextView = itemView.findViewById(R.id.studentEmail)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_unapproved, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.nameTv.text = student.name
        holder.emailTv.text = student.email

        holder.btnApprove.setOnClickListener {
            onApproveClick(student)
        }
        holder.btnReject.setOnClickListener {
            onRejectClick(student)
        }
    }

    override fun getItemCount(): Int = students.size
}
