package com.swn.hostelmanagementsystem.ui.student

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import de.hdodenhof.circleimageview.CircleImageView

class StudentDashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var textHostel: TextView
    private lateinit var textName: TextView
    private lateinit var textEmail: TextView
    private lateinit var textRoomNumber: TextView
    private lateinit var textFeeStatus: TextView
    private lateinit var noticesContainer: LinearLayout
    private lateinit var profileImage: CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        swipeRefreshLayout = view.findViewById(R.id.student_swipe_refresh)
        textName = view.findViewById(R.id.text_student_name)
        textHostel = view.findViewById(R.id.text_hostel_name)
        textEmail = view.findViewById(R.id.text_student_email)
        textRoomNumber = view.findViewById(R.id.text_room_number)
        textFeeStatus = view.findViewById(R.id.text_fee_status)
        noticesContainer = view.findViewById(R.id.notices_container)
        profileImage = view.findViewById(R.id.profile_image)

        swipeRefreshLayout.setOnRefreshListener {
            loadStudentInfo()
        }

        loadStudentInfo()

        return view
    }

    private fun loadStudentInfo() {
        val userId = auth.currentUser?.uid ?: return
        swipeRefreshLayout.isRefreshing = true

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name") ?: "Student"
                    val email = document.getString("email") ?: ""
                    val room = document.getString("roomNumber") ?: "-"
                    val feesPaid = document.getBoolean("feesPaid") ?: false
                    val profileBase64 = document.getString("profileImage")
                    val hostelId = document.getString("hostelId")

                    textName.text = name
                    textEmail.text = email
                    textRoomNumber.text = room
                    textFeeStatus.text = if (feesPaid) "Paid" else "Pending"

                    // Load Base64 profile image
                    if (!profileBase64.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(profileBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profileImage.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    // Load hostel name from hostelId
                    if (!hostelId.isNullOrEmpty()) {
                        db.collection("hostels").document(hostelId).get()
                            .addOnSuccessListener { hostelDoc ->
                                val hostelName = hostelDoc.getString("name") ?: "Unknown Hostel"
                                textHostel.text = hostelName
                            }
                            .addOnFailureListener {
                                textHostel.text = "Failed to load hostel"
                            }
                    } else {
                        textHostel.text = "No Hostel Assigned"
                    }

                    loadNotices()
                } else {
                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load student data", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun loadNotices() {
        noticesContainer.removeAllViews()

        db.collection("notices")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val notice = doc.getString("message") ?: continue

                    val noticeCard = layoutInflater.inflate(R.layout.item_notice_card, noticesContainer, false)
                    val noticeText = noticeCard.findViewById<TextView>(R.id.notice_text)
                    noticeText.text = notice
                    noticesContainer.addView(noticeCard)
                }
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load notices", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
    }
}
