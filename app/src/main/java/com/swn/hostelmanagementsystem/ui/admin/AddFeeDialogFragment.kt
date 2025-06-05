package com.swn.hostelmanagementsystem.ui.admin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.model.Fee

class AddFeeDialogFragment : DialogFragment() {

    interface FeeDialogListener {
        fun onFeeSaved()
    }

    var listener: FeeDialogListener? = null

    private var fee: Fee? = null

    private lateinit var editTitle: EditText
    private lateinit var editAmount: EditText
    private lateinit var spinnerFrequency: Spinner
    private lateinit var editDescription: EditText
    private lateinit var spinnerHostels: Spinner

    private val firestore = FirebaseFirestore.getInstance()

    private var hostelList: List<Pair<String, String>> = emptyList()
    private var selectedHostelId: String? = null

    companion object {
        fun newInstance(fee: Fee?): AddFeeDialogFragment {
            val fragment = AddFeeDialogFragment()
            val bundle = Bundle()
            bundle.putParcelable("fee", fee)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_add_fee, null)

        editTitle = view.findViewById(R.id.editFeeTitle)
        editAmount = view.findViewById(R.id.editFeeAmount)
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency)
        editDescription = view.findViewById(R.id.editFeeDescription)
        spinnerHostels = view.findViewById(R.id.spinnerHostels)

        setupFrequencySpinner()
        loadHostelsIntoSpinner()

        // Get fee from arguments if editing
        fee = arguments?.getParcelable("fee")

        if (fee != null) {
            editTitle.setText(fee!!.title)
            editAmount.setText(fee!!.amount.toString())
            editDescription.setText(fee!!.description)
            spinnerFrequency.setSelection(getFrequencyPosition(fee!!.frequency))
        }

        builder.setView(view)
            .setTitle(if (fee == null) "Add Fee" else "Edit Fee")
            .setPositiveButton("Save") { _, _ -> saveFee() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    private fun setupFrequencySpinner() {
        val freqOptions = listOf("Monthly", "Quarterly", "Half Yearly", "Yearly")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, freqOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrequency.adapter = adapter
    }

    private fun loadHostelsIntoSpinner() {
        firestore.collection("hostels").get()
            .addOnSuccessListener { result ->
                hostelList = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val id = doc.id
                    Pair(id, name)
                }

                val hostelNames = hostelList.map { it.second }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hostelNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerHostels.adapter = adapter

                spinnerHostels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedHostelId = hostelList[position].first
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedHostelId = null
                    }
                }

                // If editing, set selected hostel
                fee?.let { existingFee ->
                    val index = hostelList.indexOfFirst { it.first == existingFee.hostelId }
                    if (index >= 0) {
                        spinnerHostels.setSelection(index)
                        selectedHostelId = hostelList[index].first
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load hostels", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFrequencyPosition(freq: Int): Int {
        return when (freq) {
            1 -> 0
            3 -> 1
            6 -> 2
            12 -> 3
            else -> 0
        }
    }

    private fun saveFee() {
        val title = editTitle.text.toString().trim()
        val amountStr = editAmount.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val frequency = when (spinnerFrequency.selectedItemPosition) {
            0 -> 1
            1 -> 3
            2 -> 6
            3 -> 12
            else -> 1
        }

        if (title.isEmpty()) {
            Toast.makeText(context, "Enter title", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(context, "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedHostelId == null) {
            Toast.makeText(context, "Please select a hostel", Toast.LENGTH_SHORT).show()
            return
        }

        if (fee == null) {
            val docRef = firestore.collection("fees").document()
            val generatedId = docRef.id

            val newFee = Fee(
                id = generatedId,
                title = title,
                amount = amount,
                frequency = frequency,
                description = description,
                hostelId = selectedHostelId
            )

            docRef.set(newFee)
                .addOnSuccessListener {
                    Toast.makeText(context, "Fee added", Toast.LENGTH_SHORT).show()
                    listener?.onFeeSaved()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to add fee", Toast.LENGTH_SHORT).show()
                }

        } else {
            val updatedFee = Fee(
                id = fee!!.id,
                title = title,
                amount = amount,
                frequency = frequency,
                description = description,
                hostelId = selectedHostelId
            )

            firestore.collection("fees").document(updatedFee.id!!)
                .set(updatedFee)
                .addOnSuccessListener {
                    Toast.makeText(context, "Fee updated", Toast.LENGTH_SHORT).show()
                    listener?.onFeeSaved()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update fee", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
