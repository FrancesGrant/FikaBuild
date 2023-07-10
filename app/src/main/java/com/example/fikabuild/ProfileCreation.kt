package com.example.fikabuild

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class UserProfile(
    val username: String,
    val bio: String,
    val address: String
)

class ProfileCreation : AppCompatActivity() {
    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_creation)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextBio = findViewById<EditText>(R.id.editTextBio)
        val editTextAddress = findViewById<EditText>(R.id.editTextAddress)

        // Buttons
        val createProfile = findViewById<Button>(R.id.buttonCreateProfile)
        createProfile.setOnClickListener {
            val username = editTextUsername.text.toString()
            val bio = editTextBio.text.toString()
            val address = editTextAddress.text.toString()

            // Convert address to coordinates using Geocoding API
            convertAddressToCoordinates(address) { coordinates ->
                if (coordinates != null) {
                    val userProfile = UserProfile(username, bio, coordinates)

                    // Save the user profile data to Firestore
                    usersCollection.document(username)
                        .set(userProfile)
                        .addOnSuccessListener {
                            // Profile data saved successfully
                            val intent = Intent(this@ProfileCreation, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            // Handle the failure to save profile data
                            // Show an error message or perform appropriate actions
                        }
                } else {
                    // Failed to convert address to coordinates
                    // Show an error message or perform appropriate actions
                }
            }
        }
    }

    // Toolbar back button functionality
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

    private fun convertAddressToCoordinates(address: String, callback: (String?) -> Unit) {
        val apiKey = "AIzaSyAV-HDST6dG9xdv55CaIRIwmY2jbKXy-OE"
        val encodedAddress = Uri.encode(address)
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string()
                val coordinates = parseCoordinatesFromResponse(responseBody)
                callback(coordinates)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }
        })
    }

    private fun parseCoordinatesFromResponse(responseBody: String?): String? {
        val jsonObject = JSONObject(responseBody)
        val resultsArray = jsonObject.getJSONArray("results")

        if (resultsArray.length() > 0) {
            val resultObject = resultsArray.getJSONObject(0)
            val geometryObject = resultObject.getJSONObject("geometry")
            val locationObject = geometryObject.getJSONObject("location")
            val latitude = locationObject.getDouble("lat")
            val longitude = locationObject.getDouble("lng")
            return "$latitude,$longitude"
        }

        return null
    }
}