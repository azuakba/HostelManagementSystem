package com.swn.hostelmanagementsystem.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

class StudentAttendanceFragment : Fragment() {

    private lateinit var tvPresent: TextView
    private lateinit var tvAbsent: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val attendanceList = mutableListOf<AttendanceRecord>()
    private lateinit var adapter: StudentAttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_attendance, container, false)

        tvPresent = view.findViewById(R.id.tvPresent)
        tvAbsent = view.findViewById(R.id.tvAbsent)
        tvPercentage = view.findViewById(R.id.tvPercentage)
        rvHistory = view.findViewById(R.id.rvHistory)
        progressBar = view.findViewById(R.id.progressBar)

        adapter = StudentAttendanceAdapter(attendanceList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        loadAttendance()

        return view
    }

    private fun loadAttendance() {
        progressBar.visibility = View.VISIBLE
        val userEmail = auth.currentUser?.email ?: return

        db.collection("attendance")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { result ->
                var present = 0
                var absent = 0
                attendanceList.clear()

                for (doc in result) {
                    val date = doc.getString("date") ?: ""
                    val status = doc.getString("status") ?: "absent"
                    attendanceList.add(AttendanceRecord(date = date, status = status))

                    if (status == "present") present++
                    else absent++
                }

                val total = present + absent
                val percentage = if (total > 0) (present * 100) / total else 0

                tvPresent.text = "Present: $present"
                tvAbsent.text = "Absent: $absent"
                tvPercentage.text = "Attendance: $percentage%"

                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load attendance", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
}

data class AttendanceRecord(
    val date: String = "",
    val status: String = ""
)
