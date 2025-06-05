package com.swn.hostelmanagementsystem.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.swn.hostelmanagementsystem.databinding.ActivityEditHostelBinding
import com.swn.hostelmanagementsystem.model.Hostel

class EditHostelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditHostelBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var hostelId: String? = null
    private var hostelDocRef: DocumentReference? = null
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditHostelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hostelId = intent.getStringExtra("hostelId")
        if (hostelId == null) {
            Toast.makeText(this, "Hostel ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        hostelDocRef = firestore.collection("hostels").document(hostelId!!)

        // Start listening for live updates
        listenForHostelUpdates()

        binding.buttonUpdateHostel.setOnClickListener {
            updateHostel()
        }
    }

    private fun listenForHostelUpdates() {
        binding.progressBar.visibility = View.VISIBLE
        listenerRegistration = hostelDocRef?.addSnapshotListener { snapshot, error ->
            binding.progressBar.visibility = View.GONE
            if (error != null) {
                Toast.makeText(this, "Error loading hostel data: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val hostel = snapshot.toObject(Hostel::class.java)
                // Update UI only if data is different to avoid infinite loops
                if (binding.editTextHostelName.text.toString() != hostel?.name) {
                    binding.editTextHostelName.setText(hostel?.name)
                }
                if (binding.editTextHostelLocation.text.toString() != hostel?.location) {
                    binding.editTextHostelLocation.setText(hostel?.location)
                }
            } else {
                Toast.makeText(this, "Hostel not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateHostel() {
        val name = binding.editTextHostelName.text.toString().trim()
        val location = binding.editTextHostelLocation.text.toString().trim()

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val updatedData = mapOf(
            "name" to name,
            "location" to location
        )

        hostelDocRef?.update(updatedData)
            ?.addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Hostel updated successfully", Toast.LENGTH_SHORT).show()
                // Optionally finish() here if you want to close after update
            }
            ?.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to update hostel", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()  // Stop listening when activity is destroyed
    }
}
