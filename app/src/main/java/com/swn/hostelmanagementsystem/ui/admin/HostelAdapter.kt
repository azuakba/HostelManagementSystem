package com.swn.hostelmanagementsystem.ui.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.databinding.ItemHostelBinding
import com.swn.hostelmanagementsystem.model.Hostel

class HostelAdapter(
    private val hostelList: MutableList<Hostel>
) : RecyclerView.Adapter<HostelAdapter.HostelViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostelViewHolder {
        val binding = ItemHostelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HostelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HostelViewHolder, position: Int) {
        val hostel = hostelList[position]
        val context = holder.binding.root.context

        holder.binding.textViewHostelName.text = hostel.name
        holder.binding.textViewHostelLocation.text = hostel.location
        holder.binding.textViewCapacity.text =
            "Occupancy: ${hostel.totalOccupied} / ${hostel.totalCapacity}"

        holder.binding.buttonEdit.setOnClickListener {
            val intent = Intent(context, EditHostelActivity::class.java)
            intent.putExtra("hostelId", hostel.hostelId)
            context.startActivity(intent)
        }

        holder.binding.buttonDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Hostel")
                .setMessage("Are you sure you want to delete this hostel?")
                .setPositiveButton("Delete") { _, _ ->
                    firestore.collection("hostels")
                        .document(hostel.hostelId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Hostel deleted", Toast.LENGTH_SHORT).show()
                            hostelList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, hostelList.size)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to delete hostel", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int = hostelList.size

    inner class HostelViewHolder(val binding: ItemHostelBinding) :
        RecyclerView.ViewHolder(binding.root)
}
