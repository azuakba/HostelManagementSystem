package com.swn.hostelmanagementsystem.ui.admin

import android.R
import java.util.UUID
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.databinding.ActivityRoomManagementBinding
import com.swn.hostelmanagementsystem.model.Room

import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.swn.hostelmanagementsystem.adapter.RoomAdapter

class RoomManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomManagementBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var hostelId: String? = null
    private val hostelMap = mutableMapOf<String, String>() // hostel name -> id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerViewRooms.layoutManager= LinearLayoutManager(this)

        loadHostelsIntoSpinner()

        binding.btnAddRoom.setOnClickListener {
            val roomNum = binding.editTextRoomNumber.text.toString()
            val capacity = binding.editTextCapacity.text.toString().toIntOrNull() ?: 0

            if (roomNum.isEmpty() || capacity <= 0 || hostelId == null) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val room = Room(
                id = UUID.randomUUID().toString(),
                roomNumber = roomNum,
                capacity = capacity,
                occupied = 0,
                hostelId = hostelId!!
            )

            firestore.collection("rooms").document(room.id).set(room)
                .addOnSuccessListener {
                    Toast.makeText(this, "Room added", Toast.LENGTH_SHORT).show()
                    loadRooms()
                }
        }

        loadRooms()
    }

    private fun loadHostelsIntoSpinner() {
        firestore.collection("hostels").get().addOnSuccessListener { snapshot ->
            val hostelNames = mutableListOf<String>()
            hostelMap.clear()

            for (doc in snapshot) {
                val name = doc.getString("name") ?: continue
                val id = doc.id
                hostelNames.add(name)
                hostelMap[name] = id
            }

            val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, hostelNames)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.spinnerHostels.adapter = adapter

            binding.spinnerHostels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedName = parent.getItemAtPosition(position) as String
                    hostelId = hostelMap[selectedName]
                    loadRooms() // reload rooms for selected hostel
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    hostelId = null
                }
            }
        }
    }

    private fun loadRooms() {
        if (hostelId == null) return

        firestore.collection("rooms")
            .whereEqualTo("hostelId", hostelId)
            .get()
            .addOnSuccessListener { result ->
                val rooms = result.documents.mapNotNull { it.toObject(Room::class.java) }
                val updatedRooms = mutableListOf<Room>()

                var processed = 0

                for (room in rooms) {
                    firestore.collection("users")
                        .whereEqualTo("hostelId", hostelId)
                        .whereEqualTo("roomNumber", room.roomNumber)
                        .get()
                        .addOnSuccessListener { studentSnapshot ->
                            val occupiedCount = studentSnapshot.size()
                            room.occupied = occupiedCount // update dynamically

                            updatedRooms.add(room)
                            processed++

                            if (processed == rooms.size) {
                                updateRecyclerView(updatedRooms)
                            }
                        }
                        .addOnFailureListener {
                            processed++
                            if (processed == rooms.size) {
                                updateRecyclerView(updatedRooms)
                            }
                        }
                }

                if (rooms.isEmpty()) {
                    updateRecyclerView(emptyList())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load rooms: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRecyclerView(rooms: List<Room>) {
        Toast.makeText(this, "Rooms loaded: ${rooms.size}", Toast.LENGTH_SHORT).show()

        val adapter = RoomAdapter(rooms) { roomToDelete ->
            if (roomToDelete.occupied > 0) {
                Toast.makeText(this, "Cannot delete room. It is currently occupied.", Toast.LENGTH_SHORT).show()
            } else {
                deleteRoom(roomToDelete)
            }
        }
        binding.recyclerViewRooms.adapter = adapter
    }


    private fun deleteRoom(room: Room) {
        firestore.collection("rooms").document(room.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Room deleted", Toast.LENGTH_SHORT).show()
                loadRooms()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete room", Toast.LENGTH_SHORT).show()
            }
    }



}
