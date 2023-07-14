package com.example.fikabuild

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Represents a user's profile information.
 *
 * @property username The username of the user.
 * @property bio The biography of the user.
 * @property address The address of the user.
 *
 */
data class UserProfile(
    val username: String,
    val bio: String,
    val address: String,
)

/**
 * This activity allows users to complete their profile for the sign up process.
 *
 * The Firebase Authentication instance is used for user authentication.
 */
class ProfileCreation : AppCompatActivity() {
    // Firebase
    private val db = FirebaseFirestore.getInstance()

    /**
     * The collection reference for the "users" collection in the Firestore database.
     **/
    private val usersCollection = db.collection("users")

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_creation)

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Text inputs from user
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextBio = findViewById<EditText>(R.id.editTextBio)
        val editTextAddress = findViewById<EditText>(R.id.editTextAddress)
        // Button for creating the profile
        val createProfile = findViewById<Button>(R.id.buttonCreateProfile)

        /**
         * Sets an OnClickListener on the signUpButton to handle the sign up process.
         */
        createProfile.setOnClickListener {
            val username = editTextUsername.text.toString()
            val bio = editTextBio.text.toString()
            val address = editTextAddress.text.toString()

            val userProfile = UserProfile(username, bio, address)

            // Save the user profile data to Firestore
            usersCollection.document(username)
                .set(userProfile)
                .addOnSuccessListener {
                    // Profile data saved successfully, user brought to MapsActivity screen
                    val intent = Intent(this@ProfileCreation, MapsActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    // Error message displayed for failure to save profile data
                    Toast.makeText(
                        this@ProfileCreation,
                        "Profile failed to save, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    /**
     * Overrides the `onOptionsItemSelected` method to handle menu item selections.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@ProfileCreation, SignUp::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

