package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class StartNewFika : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_new_fika)

        // Text inputs
        val editTextLocationA = findViewById<EditText>(R.id.editTextLocationA)
        val editTextLocationB = findViewById<EditText>(R.id.editTextLocationB)

        // Buttons
        val searchButton = findViewById<Button>(R.id.searchButton)

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

                    // pass midpoint value to NewFika activity
                    val intent = Intent(this@StartNewFika, NewFika::class.java)
                    intent.putExtra("midpoint", midpoint)
                    startActivity(intent)
                } else {
                    // Handle geocoding failure
                }
            }
        }
    }
    private suspend fun convertAddressToCoordinates(userAddress: String, apiKey: String): LatLng? {
        val client = OkHttpClient()
        val url =
            "https://maps.googleapis.com/maps/api/geocode/json?address=$userAddress&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        val responseData = response.body?.string()
        response.close()

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