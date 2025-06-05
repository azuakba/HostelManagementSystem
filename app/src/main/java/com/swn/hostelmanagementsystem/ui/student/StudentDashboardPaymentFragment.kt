package com.swn.hostelmanagementsystem.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.Fee

class StudentDashboardPaymentFragment : Fragment() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var recyclerViewPending: androidx.recyclerview.widget.RecyclerView
    private lateinit var recyclerViewHistory: androidx.recyclerview.widget.RecyclerView

    private lateinit var tvTotalDue: TextView
    private lateinit var tvPaid: TextView
    private lateinit var tvPending: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val allFees = mutableListOf<Fee>()
    private val pendingFees = mutableListOf<Fee>()
    private val paidFees = mutableListOf<Fee>()

    private lateinit var pendingAdapter: FeeAdapter
    private lateinit var historyAdapter: FeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.student_dashboard_payment_fragment, container, false)

        spinnerFilter = view.findViewById(R.id.spinnerFeeFilter)
        recyclerViewPending = view.findViewById(R.id.recyclerViewPendingFees)
        recyclerViewHistory = view.findViewById(R.id.recyclerViewPaymentHistory)

        tvTotalDue = view.findViewById(R.id.tvTotalDue)
        tvPaid = view.findViewById(R.id.tvPaid)
        tvPending = view.findViewById(R.id.tvPending)

        // Setup RecyclerViews
        recyclerViewPending.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())

        pendingAdapter = FeeAdapter(requireContext(), pendingFees) { fee ->
            onPayNowClicked(fee)
        }
        historyAdapter = FeeAdapter(requireContext(), paidFees) { fee ->
            Toast.makeText(requireContext(), "Fee already paid", Toast.LENGTH_SHORT).show()
        }

        recyclerViewPending.adapter = pendingAdapter
        recyclerViewHistory.adapter = historyAdapter

        setupSpinner()

        loadFeesAndPayments()

        return view
    }

    private fun setupSpinner() {
        val options = listOf("All", "Paid", "Unpaid")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                applyFilter(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadFeesAndPayments() {
        val userId = auth.currentUser?.uid ?: return

        // Step 1: Load all fees (master list)
        firestore.collection("fees")
            .get()
            .addOnSuccessListener { feeSnapshot ->
                val feesList = mutableListOf<Fee>()
                for (doc in feeSnapshot.documents) {
                    val fee = doc.toObject(Fee::class.java)
                    if (fee != null) {
                        fee.id = doc.id
                        feesList.add(fee)
                    }
                }

                // Step 2: Load student's payments from student_fees collection
                firestore.collection("student_fees")
                    .whereEqualTo("studentId", userId)
                    .get()
                    .addOnSuccessListener { paymentSnapshot ->

                        val paymentStatusMap = paymentSnapshot.documents.associateBy(
                            { it.getString("feeId") ?: "" },  // feeId as key
                            { it.getString("status") ?: "unpaid" }  // status as value
                        )

                        allFees.clear()

                        // Step 3: Merge payment status into fees list
                        for (fee in feesList) {
                            val status = paymentStatusMap[fee.id] ?: "unpaid"
                            fee.status = status
                            allFees.add(fee)
                        }

                        updateSummary()
                        applyFilter(spinnerFilter.selectedItemPosition)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to load payment info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load fees", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateSummary() {
        val totalDue = allFees.sumOf { it.amount }
        val totalPaid = allFees.filter { it.status == "paid" }.sumOf { it.amount }
        val totalPending = totalDue - totalPaid

        tvTotalDue.text = "₹$totalDue"
        tvPaid.text = "₹$totalPaid"
        tvPending.text = "₹$totalPending"
    }

    private fun applyFilter(position: Int) {
        when (position) {
            0 -> { // All
                pendingFees.clear()
                paidFees.clear()
                paidFees.addAll(allFees.filter { it.status == "paid" })
                pendingFees.addAll(allFees.filter { it.status != "paid" })
            }
            1 -> { // Paid only
                paidFees.clear()
                paidFees.addAll(allFees.filter { it.status == "paid" })
                pendingFees.clear()
            }
            2 -> { // Unpaid only
                pendingFees.clear()
                pendingFees.addAll(allFees.filter { it.status != "paid" })
                paidFees.clear()
            }
        }
        pendingAdapter.notifyDataSetChanged()
        historyAdapter.notifyDataSetChanged()
    }

    private fun onPayNowClicked(fee: Fee) {
        val intent = Intent(requireContext(), StudentPaymentActivity::class.java)
        intent.putExtra("feeId", fee.id)
        intent.putExtra("feeType", fee.type)
        intent.putExtra("amount", fee.amount)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadFeesAndPayments() // Refresh fees and payment status when returning
    }
}
