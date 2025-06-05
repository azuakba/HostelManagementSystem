package com.swn.hostelmanagementsystem.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R

data class Notice(val id: String, val message: String, val timestamp: Timestamp)

class AdminNoticeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoticeAdapter
    private lateinit var db: FirebaseFirestore
    private val notices = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notice)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recycler_notices)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NoticeAdapter(notices,
            onEdit = { notice -> showEditDialog(notice) },
            onDelete = { notice -> deleteNotice(notice.id) }
        )
        recyclerView.adapter = adapter

        loadNotices()

        findViewById<View>(R.id.fab_add_notice).setOnClickListener {
            showAddNoticeDialog()
        }
    }

    private fun loadNotices() {
        db.collection("notices")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                notices.clear()
                for (doc in result) {
                    val message = doc.getString("message") ?: continue
                    val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    notices.add(Notice(doc.id, message, timestamp))
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showAddNoticeDialog() {
        val input = EditText(this)
        input.hint = "Enter notice message"
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(this)
            .setTitle("Add Notice")
            .setView(input)
            .setPositiveButton("Post") { _, _ ->
                val message = input.text.toString()
                if (message.isNotBlank()) {
                    val data = hashMapOf(
                        "message" to message,
                        "timestamp" to Timestamp.now()
                    )
                    db.collection("notices").add(data).addOnSuccessListener {
                        loadNotices()
                        Toast.makeText(this, "Notice added", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(notice: Notice) {
        val input = EditText(this)
        input.setText(notice.message)

        AlertDialog.Builder(this)
            .setTitle("Edit Notice")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newMsg = input.text.toString()
                if (newMsg.isNotBlank()) {
                    db.collection("notices").document(notice.id)
                        .update("message", newMsg)
                        .addOnSuccessListener {
                            loadNotices()
                            Toast.makeText(this, "Notice updated", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNotice(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Notice")
            .setMessage("Are you sure you want to delete this notice?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("notices").document(id).delete()
                    .addOnSuccessListener {
                        loadNotices()
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
