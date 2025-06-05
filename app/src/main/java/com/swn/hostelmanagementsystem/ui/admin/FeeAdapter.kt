package com.swn.hostelmanagementsystem.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.Fee

class FeeAdapter(
    private val feeList: List<Fee>,
    private val onEditClick: (Fee) -> Unit,
    private val onDeleteClick: (Fee) -> Unit
) : RecyclerView.Adapter<FeeAdapter.FeeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fee, parent, false)
        return FeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeeViewHolder, position: Int) {
        val fee = feeList[position]
        holder.bind(fee)
    }

    override fun getItemCount(): Int = feeList.size

    inner class FeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textFeeTitle)
        private val textAmount: TextView = itemView.findViewById(R.id.textFeeAmount)
        private val textFrequency: TextView = itemView.findViewById(R.id.textFeeFrequency)
        private val textDescription: TextView = itemView.findViewById(R.id.textFeeDescription)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditFee)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteFee)

        fun bind(fee: Fee) {
            textTitle.text = fee.title
            textAmount.text = "₹%.2f".format(fee.amount)
            textFrequency.text = frequencyToText(fee.frequency)
            textDescription.text = fee.description

            btnEdit.setOnClickListener { onEditClick(fee) }
            btnDelete.setOnClickListener { onDeleteClick(fee) }
        }

        private fun frequencyToText(freq: Int): String {
            return when (freq) {
                1 -> "Monthly"
                3 -> "Quarterly"
                6 -> "Half Yearly"
                12 -> "Yearly"
                else -> "$freq months"
            }
        }
    }
}
