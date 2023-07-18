package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Activity that displays starts the process for users to find coffee locations close to the midpoint between them.
 * The activity prompts the user to input the two addresses they want to meet between.
 */
class StartNewFika : AppCompatActivity() {
    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_new_fika)

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs
        val editTextLocationA = findViewById<EditText>(R.id.editTextLocationA)
        val editTextLocationB = findViewById<EditText>(R.id.editTextLocationB)

        // Buttons
        val searchButton = findViewById<Button>(R.id.searchButton)

        /**
         * Sets a click listener for the searchButton.
         * When clicked, it calls the function to calculate the midpoint and passes the midpoint to the next Activity.
         * If there is an error with addresses is given, it displays a toast message indicating that there is an error with the address supplied.
         */
        searchButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val apiKey = "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs"
                val firstLocation =
                    convertAddressToCoordinates(editTextLocationA.text.toString(), apiKey)
                val secondLocation =
                    convertAddressToCoordinates(editTextLocationB.text.toString(), apiKey)

                if (firstLocation != null && secondLocation != null) {
                    // Calculate the midpoint using the coordinates
                    val midpoint = calculateMidpoint(firstLocation, secondLocation)

                    // Pass midpoint value to NewFika activity
                    val intent = Intent(this@StartNewFika, NewFika::class.java)
                    intent.putExtra("midpoint", midpoint)
                    startActivity(intent)
                } else {
                    // Handle geocoding failure
                    Toast.makeText(this@StartNewFika, "Error with the addresses supplied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Converts a user address to geographic coordinates (latitude and longitude).
     *
     * @param userAddress The user's address to convert.
     * @param apiKey The API key for accessing the Geocoding API.
     * @return The LatLng object representing the geographic coordinates of the address, or null if the conversion fails.
     */
    private suspend fun convertAddressToCoordinates(userAddress: String, apiKey: String): LatLng? {
        val client = OkHttpClient()
        // Constructs URL for the Geocoding API request
        val url =
            "https://maps.googleapis.com/maps/api/geocode/json?address=$userAddress&key=$apiKey"
        // Builds a HTTP request
        val request = Request.Builder()
            .url(url)
            .build()
        // Executes the HTTP request asynchronously
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
        // Closes response to free up resources
        val responseData = response.body?.string()
        response.close()
        // Executes the Geocoding API request which retrieves the coordinates
        if (responseData != null) {
            val jsonObject = JSONObject(responseData)
            val results = jsonObject.getJSONArray("results")

            if (results.length() > 0) {
                val location =
                    results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                val latitude = location.getDouble("lat")
                val longitude = location.getDouble("lng")

                return LatLng(latitude, longitude)
            }
        }

        return null
    }

    /**
     * Calculates the midpoint between two locations.
     *
     * @param firstLocation The first location as a LatLng object.
     * @param secondLocation The second location as a LatLng object.
     * @return The midpoint between the two locations as a LatLng object.
     */
    private fun calculateMidpoint(firstLocation: LatLng, secondLocation: LatLng): LatLng {
        val latMid = (firstLocation.latitude + firstLocation.latitude) / 2.0
        val lngMid = (secondLocation.longitude + secondLocation.longitude) / 2.0
        return LatLng(latMid, lngMid)
    }

    /**
     * Overrides the `onOptionsItemSelected` method of the activity to handle menu item selection.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@StartNewFika, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}