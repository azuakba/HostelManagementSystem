package com.swn.hostelmanagementsystem.ui.shared

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swn.hostelmanagementsystem.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class EditProfile : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var profilePicture: CircleImageView

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        fullNameEditText = findViewById(R.id.editTextFullName)
        addressEditText = findViewById(R.id.editTextAddress)
        phoneEditText = findViewById(R.id.editTextPhone)
        emailEditText = findViewById(R.id.editTextEmail)
        bioEditText = findViewById(R.id.editTextBio)
        saveButton = findViewById(R.id.btnSaveProfile)
        profilePicture = findViewById(R.id.profilePicture) // <-- Added this line!

        // Register launchers for camera and gallery
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.extras?.getParcelable("data")
                }

                bitmap?.let {
                    profilePicture.setImageBitmap(it)
                    saveImageToFirestore(it)
                }
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                val bitmap: Bitmap? = uri?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(contentResolver, it)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(contentResolver, it)
                    }
                }

                bitmap?.let {
                    profilePicture.setImageBitmap(it)
                    saveImageToFirestore(it)
                }
            }
        }

        loadProfile()

        profilePicture.setOnClickListener {
            showImagePickerDialog()
        }

        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Set Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (checkAndRequestPermissionsForCamera()) {
                            openCamera()
                        }
                    }
                    1 -> {
                        if (checkAndRequestPermissionsForGallery()) {
                            openGallery()
                        }
                    }
                }
            }
            .show()
    }

    private fun checkAndRequestPermissionsForCamera(): Boolean {
        val cameraPermission = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            requestPermissions(arrayOf(cameraPermission), 101)
            return false
        }
    }

    private fun checkAndRequestPermissionsForGallery(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readMediaImages = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, readMediaImages) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(arrayOf(readMediaImages), 102)
                false
            }
        } else {
            val readStorage = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, readStorage) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                requestPermissions(arrayOf(readStorage), 102)
                false
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                101 -> openCamera()
                102 -> openGallery()
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun saveImageToFirestore(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val imageBytes = baos.toByteArray()
        val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        currentUserId?.let { uid ->
            db.collection("users").document(uid)
                .update("profileImage", imageBase64)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadProfile() {
        currentUserId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        fullNameEditText.setText(document.getString("fullName") ?: "")
                        addressEditText.setText(document.getString("address") ?: "")
                        phoneEditText.setText(document.getString("phone") ?: "")
                        emailEditText.setText(document.getString("email") ?: "")
                        bioEditText.setText(document.getString("bio") ?: "")

                        // Load profile image if present
                        val imageBase64 = document.getString("profileImage")
                        if (!imageBase64.isNullOrEmpty()) {
                            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profilePicture.setImageBitmap(bitmap)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
        val updatedProfile = hashMapOf(
            "fullName" to fullNameEditText.text.toString().trim(),
            "address" to addressEditText.text.toString().trim(),
            "phone" to phoneEditText.text.toString().trim(),
            "email" to emailEditText.text.toString().trim(),
            "bio" to bioEditText.text.toString().trim()
        )

        currentUserId?.let { uid ->
            db.collection("users").document(uid).update(updatedProfile as Map<String, Any>)
                .addOnSuccessListener {
                    Snackbar.make(saveButton, "Profile updated successfully", Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Snackbar.make(saveButton, "Error saving profile", Snackbar.LENGTH_LONG).show()
                }
        }
    }
}
