package com.swn.hostelmanagementsystem.ui.student

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.Fee

class FeeAdapter(
    private val context: Context,
    private val feeList: List<Fee>,
    private val onPayClick: (Fee) -> Unit
) : RecyclerView.Adapter<FeeAdapter.FeeViewHolder>() {

    inner class FeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvFeeTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvFeeDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvFeeAmount)
        val btnPay: Button = itemView.findViewById(R.id.btnPayNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.student_item_fee, parent, false)
        return FeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeeViewHolder, position: Int) {
        val fee = feeList[position]

        holder.tvTitle.text = fee.title
        holder.tvDesc.text = fee.description
        holder.tvAmount.text = "₹${fee.amount}"

        holder.btnPay.setOnClickListener {
            onPayClick(fee)
        }
    }

    override fun getItemCount(): Int = feeList.size
}
