package com.swn.hostelmanagementsystem.ui.admin
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import com.swn.hostelmanagementsystem.ui.shared.EditProfile
import de.hdodenhof.circleimageview.CircleImageView

class AdminDashboardProfileActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var addressField: EditText
    private lateinit var phoneField: EditText
    private lateinit var emailField: EditText
    private lateinit var bioField: EditText
    private lateinit var editProfileButton: Button
    private lateinit var profilePicture: CircleImageView



    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_dashboard_profile)

        // Initialize views
        profilePicture = findViewById(R.id.profilePicture)
        nameField = findViewById(R.id.nameField)
        addressField = findViewById(R.id.addressField)
        phoneField = findViewById(R.id.phoneField)
        emailField = findViewById(R.id.emailField)
        bioField = findViewById(R.id.bioField)
        editProfileButton = findViewById(R.id.editProfileButton)



        loadAdminProfile()



        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }
    }


    private fun loadAdminProfile() {
        currentUserId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nameField.setText(document.getString("fullName") ?: "")
                        addressField.setText(document.getString("address") ?: "")
                        phoneField.setText(document.getString("phone") ?: "")
                        emailField.setText(document.getString("email") ?: "")
                        bioField.setText(document.getString("bio") ?: "")

                        val imageBase64 = document.getString("profileImage")
                        if (!imageBase64.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profilePicture.setImageBitmap(bitmap)
                        }
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
