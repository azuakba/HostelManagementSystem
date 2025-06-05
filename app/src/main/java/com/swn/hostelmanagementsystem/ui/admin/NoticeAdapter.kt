package com.swn.hostelmanagementsystem.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.R
import java.text.SimpleDateFormat
import java.util.*

class NoticeAdapter(
    private val noticeList: List<Notice>,
    private val onEdit: (Notice) -> Unit,
    private val onDelete: (Notice) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.bind(notice)
    }

    override fun getItemCount() = noticeList.size

    inner class NoticeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val message: TextView = view.findViewById(R.id.text_notice_message)
        private val timestamp: TextView = view.findViewById(R.id.text_notice_timestamp)
        private val editBtn: ImageButton = view.findViewById(R.id.btn_edit_notice)
        private val deleteBtn: ImageButton = view.findViewById(R.id.btn_delete_notice)

        fun bind(notice: Notice) {
            message.text = notice.message
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            timestamp.text = sdf.format(notice.timestamp.toDate())

            editBtn.setOnClickListener { onEdit(notice) }
            deleteBtn.setOnClickListener { onDelete(notice) }
        }
    }
}
