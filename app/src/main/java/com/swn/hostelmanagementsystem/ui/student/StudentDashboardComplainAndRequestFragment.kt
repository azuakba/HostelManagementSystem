package com.swn.hostelmanagementsystem.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.swn.hostelmanagementsystem.databinding.StudentDashboardComplainandrequestBinding

class StudentDashboardComplainAndRequestFragment : Fragment() {

    private var _binding: StudentDashboardComplainandrequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RequestAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = StudentDashboardComplainandrequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Spinner with request types
        val types = listOf("Complaint", "Request")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = spinnerAdapter

        adapter = RequestAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        loadRequests()

        binding.submitRequestBtn.setOnClickListener {
            val type = binding.spinnerType.selectedItem.toString()
            val description = binding.inputDescription.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔽 Get the student's name from Firestore
            firestore.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val studentName = document.getString("name") ?: "Unknown"

                        val request = hashMapOf(
                            "studentId" to currentUserId,
                            "studentName" to studentName,
                            "type" to type,
                            "description" to description,
                            "timestamp" to System.currentTimeMillis()
                        )

                        firestore.collection("requests")
                            .add(request)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Request submitted", Toast.LENGTH_SHORT).show()
                                binding.inputDescription.setText("")
                                loadRequests()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to submit request", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch student name", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun loadRequests() {
        val studentId = auth.currentUser?.uid
        if (studentId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("requests")
            .whereEqualTo("studentId", studentId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val requests = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RequestItem::class.java)
                }
                adapter.submitList(requests)
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
