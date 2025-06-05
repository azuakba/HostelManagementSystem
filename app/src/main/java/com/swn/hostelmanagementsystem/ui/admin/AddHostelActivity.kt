package com.swn.hostelmanagementsystem.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.databinding.ActivityAddHostelBinding
import com.swn.hostelmanagementsystem.model.Hostel
import java.util.*

class AddHostelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHostelBinding
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHostelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddHostel.setOnClickListener {
            val name = binding.editTextHostelName.text.toString().trim()
            val location = binding.editTextHostelLocation.text.toString().trim()

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hostelId = UUID.randomUUID().toString()
            val hostel = Hostel(hostelId = hostelId, name = name, location = location)

            firestore.collection("hostels").document(hostelId).set(hostel)
                .addOnSuccessListener {
                    Toast.makeText(this, "Hostel added", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add hostel", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
