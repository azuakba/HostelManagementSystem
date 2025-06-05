package com.swn.hostelmanagementsystem.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.databinding.ActivityHostelManagementBinding
import com.swn.hostelmanagementsystem.model.Hostel

class HostelManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostelManagementBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val hostelList = mutableListOf<Hostel>()
    private lateinit var adapter: HostelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHostelManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HostelAdapter(hostelList)
        binding.recyclerViewHostels.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHostels.adapter = adapter

        binding.buttonAddHostel.setOnClickListener {
            startActivity(Intent(this, AddHostelActivity::class.java))
        }

        fetchHostels()
    }

    private fun fetchHostels() {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("hostels")
            .get()
            .addOnSuccessListener { snapshot ->

                val hostelMap = mutableMapOf<String, Hostel>()  // Use a map to prevent duplicates

                val hostels = snapshot.documents.mapNotNull { it.toObject(Hostel::class.java)?.apply { hostelId = it.id } }
                val totalHostels = hostels.size
                var processedHostels = 0

                if (totalHostels == 0) {
                    hostelList.clear()
                    adapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                    return@addOnSuccessListener
                }

                for (hostel in hostels) {
                    // Step 1: Fetch rooms for this hostel
                    firestore.collection("rooms")
                        .whereEqualTo("hostelId", hostel.hostelId)
                        .get()
                        .addOnSuccessListener { roomSnapshot ->
                            var totalCapacity = 0
                            for (roomDoc in roomSnapshot.documents) {
                                val capacity = roomDoc.getLong("capacity")?.toInt() ?: 0
                                totalCapacity += capacity
                            }
                            hostel.totalCapacity = totalCapacity

                            // Step 2: Fetch students assigned to rooms in this hostel
                            firestore.collection("users")
                                .whereEqualTo("hostelId", hostel.hostelId) // only students assigned rooms
                                .get()
                                .addOnSuccessListener { studentSnapshot ->
                                    val totalOccupied = studentSnapshot.documents.count { doc ->
                                        val roomNum = doc.getString("roomNumber")
                                        !roomNum.isNullOrEmpty()
                                    }
                                    hostel.totalOccupied = totalOccupied

                                    // Add or update in map by hostelId
                                    hostelMap[hostel.hostelId] = hostel

                                    processedHostels++

                                    // When all hostels processed, update UI
                                    if (processedHostels == totalHostels) {
                                        hostelList.clear()
                                        hostelList.addAll(hostelMap.values.sortedBy { it.name })
                                        adapter.notifyDataSetChanged()
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Student fetch failed", Toast.LENGTH_SHORT).show()
                                    // Even if student fetch fails, proceed with capacity info
                                    hostelMap[hostel.hostelId] = hostel
                                    processedHostels++
                                    if (processedHostels == totalHostels) {
                                        hostelList.clear()
                                        hostelList.addAll(hostelMap.values.sortedBy { it.name })
                                        adapter.notifyDataSetChanged()
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                        }
                        .addOnFailureListener {
                            processedHostels++
                            if (processedHostels == totalHostels) {
                                hostelList.clear()
                                hostelList.addAll(hostelMap.values.sortedBy { it.name })
                                adapter.notifyDataSetChanged()
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load hostels", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }





    override fun onResume() {
        super.onResume()
        fetchHostels() // refresh after returning from AddHostelActivity
    }
}
