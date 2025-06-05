package com.swn.hostelmanagementsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swn.hostelmanagementsystem.databinding.ItemRoomBinding
import com.swn.hostelmanagementsystem.model.Room

class RoomAdapter(
    private val rooms: List<Room>,
    private val onDeleteClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.binding.roomNumberText.text = "Room: ${room.roomNumber}"
        holder.binding.capacityText.text = "Capacity: ${room.capacity}"
        holder.binding.occupiedText.text = "Occupied: ${room.occupied}"

        holder.binding.btnDeleteRoom.setOnClickListener {
            onDeleteClick(room)
        }
    }

    override fun getItemCount(): Int = rooms.size
}
