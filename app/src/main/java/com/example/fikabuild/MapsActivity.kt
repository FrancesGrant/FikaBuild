package com.example.fikabuild

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fikabuild.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * This activity represents the main map screen of the app.
 * It allows users to view and interact with the map, as well as access other screens.
 *
 * Implements [OnMapReadyCallback] to handle map initialization and interaction. */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val locationPermissionRequestCode = 123 // Request code for location permission
    private lateinit var mMap: GoogleMap // Google map object for displaying the map
    private lateinit var binding: ActivityMapsBinding // Binding object for the activity_maps.xml layout
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Client for retrieving the user's current location
    private lateinit var locationRequest: LocationRequest // Request object for location updates
    private lateinit var locationCallback: LocationCallback // Callback used for receiving location updates

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflates the layout file so it can be used.
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if location permission is granted.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission granted from user.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
        }

        // Initialises a SupportMapFragment by finding the layout using its ID.
        // getMapAsync retrieves the Google Map object asynchronously and notifies when complete.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Buttons
        val coffeeNearMeButton = findViewById<Button>(R.id.buttonCoffeeNearMe)
        val newFikaButton = findViewById<Button>(R.id.buttonNewFika)
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        val favouritesButton = findViewById<ImageButton>(R.id.favouritesButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        /**
         * Sets an OnClickListener on the coffeeNearMe button to start the functionality that finds coffee near the users current location.
         */
        coffeeNearMeButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, CoffeeNearMe::class.java)
            startActivity(intent)
        }

        /**
         * Sets an OnClickListener on the newFikaButton button to start the functionality that finds coffee places between two addresses.
         */
        newFikaButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, StartNewFika::class.java)
            startActivity(intent)
        }

        /**
         * Sets an OnClickListener on the searchButton to start the functionality that allows users to search for coffee places.
         */
        searchButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, SearchScreen::class.java)
            startActivity(intent)
        }

        /**
         * Sets an OnClickListener on the favouritesButton to start the functionality that allows users to review their favourite coffee places.
         */
        favouritesButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, FavouriteScreen::class.java)
            startActivity(intent)
        }

        /**
         * Sets an OnClickListener on the profileButton to start the functionality that allows users to review and update their profile.
         */
        profileButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, ProfileScreen::class.java)
            startActivity(intent)
        }

        /**
         * Initializes the location-related variables and objects.
         *
         * - [fusedLocationClient]: Used to access the user's location.
         * - [locationRequest]: Defines the parameters for location updates.
         *   - [interval]: The interval between location updates in milliseconds.
         *   - [fastestInterval]: The minimum interval between updates, even if a more frequent update is requested.
         *   - [priority]: The priority level for location accuracy.
         *     - [LocationRequest.PRIORITY_HIGH_ACCURACY]: Requests the highest level of accuracy using GPS, Wi-Fi, and other sensors.
         */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        /**
         * Callback object for receiving location updates.
         *
         * This callback is invoked when a new location is received.
         * It processes the location data and adds a marker on the map at the user's location and moves the camera to the user's location.
         *
         * @param locationResult The received location result containing one or more locations.
         */
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    // Create a custom marker icon with black color
                    val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)

                    // Create a marker options object and set its properties
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("User's Location")
                        .snippet("Additional information about the location")
                        .icon(markerIcon)

                    // Add the marker to the map
                    mMap.addMarker(markerOptions)
                    // Moves the camera to the users location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        }
    }

    /**
     * Callback method for the result of a permission request.
     *
     * This method is invoked when the user responds to a permission request dialog.
     *
     * @param requestCode The unique code that identifies the permission request.
     * @param permissions The array of requested permissions.
     * @param grantResults The array indicating whether the permissions were granted or denied.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if request code matches the location permission request code
        if (requestCode == locationPermissionRequestCode) {
            // Check if permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            } else {
                // If permission is denied generate the toast message for the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Callback method triggered when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the map instance.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Loads custom map style
        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light)
        googleMap.setMapStyle(mapStyleOptions)

        // Checks is ACCESS_COARSE_LOCATION permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Access to location permission granted
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    // Create a custom marker icon with black color
                    val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)

                    // Create a marker options object and set its properties
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("User's Location")
                        .snippet("Additional information about the location")
                        .icon(markerIcon)

                    // Add the marker to the map
                    mMap.addMarker(markerOptions)
                    // Move camera to users location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        } else {
            // Asks the user for the ACCESS_COARSE_LOCATION permission again
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode)
            Toast.makeText(this, "Please grant location permission to use the app.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     *
     * @param menu The menu to populate with menu items.
     * @return true if the menu is to be displayed, false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Adds a "Logout" item to the menu
        menu.add("Logout")
        return true
    }

    /**
     * Callback method triggered when a menu item is selected.
     *
     * @param item The selected menu item.
     * @return true if the menu item selection is handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "Logout") {
            // Log out the user using Firebase authentication
            Firebase.auth.signOut()
            // Navigate user to the login screen
            navigateToLogin()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Navigates the user to the login screen.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, WelcomeScreen::class.java)
        // Add flags to clear the activity stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        // Start the WelcomeScreen activity
        startActivity(intent)
        // Finish the current activity to remove it from the stack
        finish()
    }

    /**
     * Called when the activity is resumed and becomes visible to the user.
     * It is responsible for starting location updates.
     */
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    /**
     * Called when the activity is paused and is no longer visible to the user.
     * It is responsible for stopping location updates.
     */
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    /**
     * Starts the location updates if the ACCESS_COARSE_LOCATION permission is granted.
     */
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates from fusedLocationClient
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    /**
     * Stops the ongoing location updates.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
