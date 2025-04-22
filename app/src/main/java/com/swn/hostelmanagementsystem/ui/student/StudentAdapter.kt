package com.swn.hostelmanagementsystem.ui.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.swn.hostelmanagementsystem.R  // Ensure correct package import

class StudentAdapter(private val studentList: MutableList<Student>) :
    RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.studentName)
        val emailTextView: TextView = itemView.findViewById(R.id.studentEmail)
        val approveButton: Button = itemView.findViewById(R.id.approveButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.nameTextView.text = student.name
        holder.emailTextView.text = student.email
        holder.approveButton.isEnabled = !student.isApproved

        holder.approveButton.setOnClickListener {
            if (!student.isApproved) {  // Prevent redundant updates
                updateStudentApproval(student.id, true, position)
            }
        }

        holder.deleteButton.setOnClickListener {
            deleteStudent(student.id, position)
        }
    }

    override fun getItemCount() = studentList.size

    private fun updateStudentApproval(studentId: String, isApproved: Boolean, position: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(studentId)
            .update("isApproved", isApproved)
            .addOnSuccessListener {
                Log.d("Firestore", "Student approval updated")

                // Update local list and refresh UI
                studentList[position].isApproved = isApproved
                notifyItemChanged(position)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating student", e)
            }
    }

    private fun deleteStudent(studentId: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(studentId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Student deleted")

                // Remove from local list and update UI
                studentList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting student", e)
            }
    }
}
