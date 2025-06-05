package com.swn.hostelmanagementsystem.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.StudentFeeStatus

class StudentFeeStatusAdapter(private val list: List<StudentFeeStatus>) :
    RecyclerView.Adapter<StudentFeeStatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentName: TextView = itemView.findViewById(R.id.tvStudentName)
        val feeStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_fee_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val item = list[position]
        holder.studentName.text = item.name

        val hasPaidAll = item.unpaidFees.isEmpty()

        holder.feeStatus.text = when {
            hasPaidAll -> "All Fees Paid"
            item.paidFees.isEmpty() -> "Unpaid: ${item.unpaidFees.joinToString(", ")}"
            else -> "Paid: ${item.paidFees.joinToString(", ")}, Unpaid: ${item.unpaidFees.joinToString(", ")}"
        }

        holder.feeStatus.setTextColor(
            holder.itemView.context.getColor(
                if (hasPaidAll) R.color.green else R.color.red
            )
        )
    }


    override fun getItemCount(): Int = list.size
}
