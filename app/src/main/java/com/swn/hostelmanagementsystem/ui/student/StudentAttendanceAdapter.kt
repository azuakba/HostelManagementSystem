package com.swn.hostelmanagementsystem.ui.student

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R

class StudentAttendanceAdapter(private val attendanceList: List<AttendanceRecord>) :
    RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = attendanceList[position]
        holder.tvDate.text = record.date
        holder.tvStatus.text = record.status.capitalize()

        holder.tvStatus.setTextColor(
            when (record.status) {
                "present" -> Color.parseColor("#2E7D32") // Green
                "absent" -> Color.RED
                else -> Color.GRAY
            }
        )
    }

    override fun getItemCount(): Int = attendanceList.size
}
