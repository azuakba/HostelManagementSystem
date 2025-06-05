package com.swn.hostelmanagementsystem.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.databinding.ItemRequestBinding
import java.text.SimpleDateFormat
import java.util.*

class RequestAdapter :
    ListAdapter<RequestItem, RequestAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RequestItem) {
            binding.tvType.text = item.type
            binding.tvDescription.text = item.description
            binding.tvTimestamp.text = formatTimestamp(item.timestamp)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class RequestDiffCallback : DiffUtil.ItemCallback<RequestItem>() {
        override fun areItemsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
            // Use timestamp + studentId for uniqueness
            return oldItem.timestamp == newItem.timestamp && oldItem.studentId == newItem.studentId
        }

        override fun areContentsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean {
            return oldItem == newItem
        }
    }
}
