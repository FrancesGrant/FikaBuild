package com.example.fikabuild

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fikabuild.databinding.ActivityCoffeeNearMeBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
/**
 * Data class representing a cafe.
 *
 * @property name The name of the cafe.
 * @property latitude The latitude coordinate of the cafe's location.
 * @property longitude The longitude coordinate of the cafe's location.
 */
data class Cafe(val name: String, val latitude: Double, val longitude: Double)

/**
 * Activity that displays nearby coffee locations.
 * It allows users to find coffee near them and receive directions.
 */
open class CoffeeNearMe : AppCompatActivity() {

    private val locationPermissionRequestCode = 123 // Request code for location permission
    private lateinit var mMap: GoogleMap // Google map object for displaying the map
    private lateinit var binding: ActivityCoffeeNearMeBinding // Binding object for the activity_coffee_near_me.xml layout
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Client for retrieving the user's current location
    private lateinit var locationRequest: LocationRequest // Request object for location updates
    private lateinit var locationCallback: LocationCallback // Callback used for receiving location updates
    private lateinit var placesClient: PlacesClient // Client for interacting with Places API
    private var selectedCafe: Cafe? = null // Variable to store selected cafe by user


    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialises the Places API with the API key and creates a PlacesClient instance.
        Places.initialize(applicationContext, "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs")
        placesClient = Places.createClient(this)

        // Inflates the layout file using the generated binding class.
        binding = ActivityCoffeeNearMeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialises a SupportMapFragment by finding the layout using its ID.
        // getMapAsync retrieves the Google Map object asynchronously and notifies when complete.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            onMapReady(googleMap)
        }

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)

        // Buttons
        val getDirectionButton = findViewById<Button>(R.id.getDirectionsButton)

        /**
         * Sets a click listener for the getDirectionButton.
         * When clicked, it launches Google Maps with the selected marker's coordinates as the destination.
         * If no cafe is selected, it displays a toast message indicating that no cafe is selected.
         */
        getDirectionButton.setOnClickListener{
            val cafe = selectedCafe
            if (cafe != null){
                // launch Google Maps with the selected marker's coordinates as the destination
                val destinationUri = Uri.parse("google.navigation:q=${cafe.latitude},${cafe.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, destinationUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } else {
                Toast.makeText(this@CoffeeNearMe, "No cafe selected", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * Sets a click listener for the toolbarTitle in the toolbar.
         * When clicked, it loads the MapsActivity screen which acts as the user's homepage.
         */
        toolbarTitle.setOnClickListener{
            val intent = Intent(this@CoffeeNearMe, MapsActivity::class.java)
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
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)
            .setFastestInterval(2000)

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
                    // Creates a marker using the coordinates of the user's location
                    val userLocation = LatLng(location.latitude, location.longitude)
                    val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("User's Location")
                        .snippet("Additional information about the location")
                        .icon(markerIcon)
                    // Adds marker to map
                    mMap.addMarker(markerOptions)
                    // Moves camera to marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    // Call function findNearbyCafes and pass userLocation as a parameter
                    findNearbyCafes(userLocation)
                }
            }
        }
    }

    /**
     * Callback method triggered when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the map instance.
     */
    private fun onMapReady(googleMap: GoogleMap) {
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
            // Retrieves the last known location from the user
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    // Create a custom marker icon with black color
                    val markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)
                    // Create a marker options object and set its properties
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("User's Location")
                        .snippet("Additional information about the location")
                        .icon(markerIcon)
                    // Add the marker to the map
                    mMap.addMarker(markerOptions)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    // Calls the function to find nearby cafes
                    findNearbyCafes(userLocation)
                }
            }
        } else {
            // Requests ACCESS_COARSE_LOCATION from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    /**
     * Finds nearby cafes using the user's location and displays them.
     *
     * @param userLocation The user's location as a LatLng object.
     */
    private fun findNearbyCafes(userLocation: LatLng) {
        // Url to make the request to Google Places API for nearby cafes
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${userLocation.latitude},${userLocation.longitude}&radius=1000&type=cafe&key=AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs"
        // Perform the HTTP request to the Google Places API
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        /**
         * Performs an asynchronous HTTP request and handles the response using a callback.
         *
         * @param request The prepared HTTP request.
         */
        client.newCall(request).enqueue(object : Callback {
            /**
             * Called when a response is received from the server.
             *
             * @param call The Call object representing the HTTP request.
             * @param response The Response object containing the response data.
             */
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Calls parseCafes function that converts the JSON response into Cafe objects
                val cafes = parseCafes(responseBody)
                runOnUiThread {
                    showCafes(cafes)
                }
            }

            /**
             * Handles the failure of the HTTP request to fetch cafes.
             * User receives the toast message indicating the failure.
             *
             * @param call The Call object representing the HTTP request.
             * @param e The IOException indicating the cause of the failure.
             */
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CoffeeNearMe, "Failed to fetch cafes", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Parses the response body containing cafe data and returns a list of cafes.
     *
     * @param responseBody The response body string obtained from the API call.
     * @return The list of cafes parsed from the response body.
     */
    private fun parseCafes(responseBody: String?): List<Cafe> {
        // List to hold objects of type 'Cafe'
        val cafes = mutableListOf<Cafe>()
        responseBody?.let {
            val jsonObject = JSONObject(it)
            val resultsArray = jsonObject.getJSONArray("results")
            // Iterates over the resultsArray and extracts values from the JSON objects and create Cafe instances with them
            // Cafe instances are added to the Cafes list
            for (i in 0 until resultsArray.length()) {
                val resultObject = resultsArray.getJSONObject(i)
                val name = resultObject.getString("name")
                val locationObject =
                    resultObject.getJSONObject("geometry").getJSONObject("location")
                val latitude = locationObject.getDouble("lat")
                val longitude = locationObject.getDouble("lng")
                cafes.add(Cafe(name, latitude, longitude))
            }
        }
        return cafes
    }

    /**
     * Displays markers on the map for each cafe in the list.
     *
     * @param cafes The list of cafes to be displayed.
     */
    private fun showCafes(cafes: List<Cafe>) {
        for (cafe in cafes) {
            // Variable to hold the location of the cafes
            val cafeLatLng = LatLng(cafe.latitude, cafe.longitude)
            // Create a marker options object and set its properties
            val markerOptions = MarkerOptions()
                .position(cafeLatLng)
                .title(cafe.name)
            // Add marker to the map
            val marker = mMap.addMarker(markerOptions)
            // Set the marker's tag as the cafe object
            if (marker != null) {
                marker.tag = cafe
            }
        }
        // Set the marker click listener
        mMap.setOnMarkerClickListener { marker ->
            selectedCafe = marker.tag as? Cafe
            marker.showInfoWindow()
            true
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
            // If location permission is granted map is initialised
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync { googleMap ->
                    onMapReady(googleMap)
                }
            } else {
                // If permission is denied generate the toast message for the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
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

    /**
     * Overrides the `onOptionsItemSelected` method of the activity to handle menu item selection.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@CoffeeNearMe, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}