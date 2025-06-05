package com.swn.hostelmanagementsystem.ui.admin

import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.Fee
import com.swn.hostelmanagementsystem.model.StudentFeeStatus
import java.text.SimpleDateFormat
import java.util.*

class FeeManagement : AppCompatActivity(), AddFeeDialogFragment.FeeDialogListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeeAdapter
    private lateinit var fabAddFee: FloatingActionButton
    private lateinit var studentStatusRecyclerView: RecyclerView
    private lateinit var studentStatusAdapter: StudentFeeStatusAdapter

    private val feeList = mutableListOf<Fee>()
    private val studentStatusList = mutableListOf<StudentFeeStatus>()
    private val firestore = FirebaseFirestore.getInstance()

    private var selectedMonth = 5  // June (0-indexed)
    private var selectedYear = 2025

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_dashboard_fee)

        val tvSelectedMonth = findViewById<TextView>(R.id.tvSelectedMonth)
        val btnPickMonth = findViewById<Button>(R.id.btnPickMonth)
        updateMonthLabel(tvSelectedMonth)

        btnPickMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            val dpd = DatePickerDialog(
                this,
                { _, y, m, _ ->
                    selectedYear = y
                    selectedMonth = m
                    updateMonthLabel(tvSelectedMonth)
                    loadStudentFeeStatus()
                },
                year, month, 1
            )

            dpd.datePicker.findViewById<View>(
                Resources.getSystem().getIdentifier("day", "id", "android")
            )?.visibility = View.GONE

            dpd.show()
        }

        // Set up main fee list
        recyclerView = findViewById(R.id.recyclerViewFees)
        fabAddFee = findViewById(R.id.fabAddFee)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FeeAdapter(feeList,
            onEditClick = { fee -> showAddEditDialog(fee) },
            onDeleteClick = { fee -> deleteFee(fee) }
        )
        recyclerView.adapter = adapter

        fabAddFee.setOnClickListener {
            showAddEditDialog(null)
        }

        // Set up student payment status list
        studentStatusRecyclerView = findViewById(R.id.recyclerViewStudentStatus)
        studentStatusRecyclerView.layoutManager = LinearLayoutManager(this)
        studentStatusAdapter = StudentFeeStatusAdapter(studentStatusList)
        studentStatusRecyclerView.adapter = studentStatusAdapter

        // Load both lists
        loadFees()
        loadStudentFeeStatus()
    }

    private fun updateMonthLabel(tv: TextView) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, selectedMonth)
        calendar.set(Calendar.YEAR, selectedYear)
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tv.text = format.format(calendar.time)
    }

    private fun loadFees() {
        firestore.collection("fees").get()
            .addOnSuccessListener { snapshot ->
                feeList.clear()
                for (doc in snapshot.documents) {
                    val fee = doc.toObject(Fee::class.java)
                    if (fee != null) {
                        fee.id = doc.id
                        feeList.add(fee)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load fees", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadStudentFeeStatus() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.time

        // Fetch all fees created by admin
        firestore.collection("fees").get()
            .addOnSuccessListener { feesSnapshot ->
                val allFees = feesSnapshot.documents.mapNotNull { it.toObject(Fee::class.java)?.apply { id = it.id } }

                // Fetch all students
                firestore.collection("users")
                    .whereEqualTo("role", "student")
                    .get()
                    .addOnSuccessListener { usersSnapshot ->
                        studentStatusList.clear()
                        var loadedCount = 0
                        val totalStudents = usersSnapshot.size()

                        for (userDoc in usersSnapshot.documents) {
                            val studentId = userDoc.id
                            val studentName = userDoc.getString("name") ?: "Unnamed"

                            firestore.collection("payments")
                                .whereEqualTo("studentId", studentId)
                                .whereEqualTo("status", "paid")
                                .whereGreaterThanOrEqualTo("timestamp", startDate)
                                .whereLessThanOrEqualTo("timestamp", endDate)
                                .get()
                                .addOnSuccessListener { paymentSnapshot ->
                                    val paidFeeIds = paymentSnapshot.documents.mapNotNull { it.getString("feeId") }

                                    val paidFees = allFees.filter { it.id in paidFeeIds }.map { it.title }
                                    val unpaidFees = allFees.filter { it.id !in paidFeeIds }.map { it.title }

                                    studentStatusList.add(
                                        StudentFeeStatus(
                                            studentId = studentId,
                                            name = studentName,
                                            paidFees = paidFees,
                                            unpaidFees = unpaidFees
                                        )
                                    )
                                }
                                .addOnCompleteListener {
                                    loadedCount++
                                    if (loadedCount == totalStudents) {
                                        // Only update UI once all data is loaded
                                        studentStatusAdapter.notifyDataSetChanged()
                                    }
                                }
                        }
                    }
            }
    }



    private fun showAddEditDialog(fee: Fee?) {
        val dialog = AddFeeDialogFragment.newInstance(fee)
        dialog.listener = this
        dialog.show(supportFragmentManager, "AddEditFeeDialog")
    }

    private fun deleteFee(fee: Fee) {
        firestore.collection("fees").document(fee.id!!).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Fee deleted", Toast.LENGTH_SHORT).show()
                loadFees()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete fee", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onFeeSaved() {
        loadFees()
    }
}
