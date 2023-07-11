package com.example.fikabuild

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class LogIn : AppCompatActivity() {

    // Class-level variables
    private lateinit var auth: FirebaseAuth
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Firebase
        auth = Firebase.auth

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)

        // Buttons
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Initialize the ActivityResultLauncher
        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    // Handle the selected image data here and upload it to Firebase Storage
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        // Upload the image to Firebase Storage and assign the URL to the user in the Firebase Cloud Database
                        uploadImageToFirebaseStorage(selectedImageUri)
                    }
                }
            }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val intent = Intent(this@LogIn, MainActivity::class.java)
                        imageActivityResultLauncher.launch(intent)
                    } else {
                        // Sign-up failed, handle the error
                        Toast.makeText(this@LogIn, "Login unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/${auth.currentUser?.uid}")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val profilePictureUrl = downloadUri.toString()

                // Update the user's profile with the profilePictureUrl in Firestore
                updateProfilePictureInFirestore(profilePictureUrl)
            }.addOnFailureListener { e ->
                // Handle the failure to get the download URL
                Toast.makeText(this@LogIn, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            // Handle the failure to upload the image
            Toast.makeText(this@LogIn, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfilePictureInFirestore(profilePictureUrl: String) {
        val user = auth.currentUser
        val firestore = Firebase.firestore

        user?.uid?.let { userId ->
            val userRef = firestore.collection("users").document(userId)
            userRef.update("profile_picture", profilePictureUrl)
                .addOnSuccessListener {
                    // Profile picture updated successfully in Firestore
                }
                .addOnFailureListener { e ->
                    // Handle the failure to update the profile picture in Firestore
                }
        }
    }

    // Toolbar back button functionality
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@LogIn, WelcomeScreen::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}





