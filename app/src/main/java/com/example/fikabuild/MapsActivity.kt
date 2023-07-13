package com.example.fikabuild

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.fikabuild.databinding.ActivityMapsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //Buttons
        val myFikaButton = findViewById<Button>(R.id.buttonMyFika)
        val newFikaButton = findViewById<Button>(R.id.buttonNewFika)
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        val notificationButton = findViewById<ImageButton>(R.id.notificationButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        // Button to launch 'My Fika' screen for user
        myFikaButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, MyFikas::class.java)
            startActivity(intent)
        }

        // Button to launch 'New Fika' screen for user.
        newFikaButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, NewFika::class.java)
            startActivity(intent)
        }

        // Button to launch 'Search' screen for user.
        searchButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, SearchScreen::class.java)
            startActivity(intent)
        }

        // Button to launch 'Notification' screen for user.
        notificationButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, NotificationScreen::class.java)
            startActivity(intent)
        }

        // Button to launch 'Profile' screen for user.
        profileButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, ProfileScreen::class.java)
            startActivity(intent)
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Get the current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Retrieve the coordinates from Firestore
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Retrieve the coordinates from the document
                        val coordinatesString = document.getString("coordinates")

                        if (!coordinatesString.isNullOrEmpty()) {
                            // Parse the latitude and longitude values from the coordinates string
                            val coordinates = coordinatesString.split(",")
                            if (coordinates.size == 2) {
                                val latitude = coordinates[0].toDoubleOrNull()
                                val longitude = coordinates[1].toDoubleOrNull()

                                if (latitude != null && longitude != null) {
                                    // Create a LatLng object with the retrieved coordinates
                                    val userLocation = LatLng(latitude, longitude)

                                    // Add a marker at the user's location and move the camera
                                    mMap.addMarker(MarkerOptions().position(userLocation).title("User's Location"))
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))
                                } else {
                                    Toast.makeText(this@MapsActivity, "Invalid coordinates format", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@MapsActivity, "Invalid coordinates format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this@MapsActivity, "Address retrieval unsuccessful", Toast.LENGTH_SHORT).show()
                }
        }
    }








    /**
     * Overrides the `onCreateOptionsMenu' method of the activity to initialize and set up the menu.
     *
     * @param menu The menu being initialized.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Logout")
        return true
    }

    /**
     * Overrides the `onOptionsItemSelected` method of the activity to handle menu item selection.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "Logout") {
            Firebase.auth.signOut()
            navigateToLogin()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Navigates the user to Login Activity.
     *
     */
    private fun navigateToLogin() {
        val intent = Intent(this, WelcomeScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}