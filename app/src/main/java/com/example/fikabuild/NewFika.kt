package com.example.fikabuild

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fikabuild.databinding.ActivityNewFikaBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class NewFika : AppCompatActivity(), OnMapReadyCallback {

    private val locationPermissionRequestCode = 123 // Request code for location permission
    private lateinit var mMap: GoogleMap // Google map object for displaying the map
    private lateinit var binding: ActivityNewFikaBinding // Binding object for the activity_coffee_near_me.xml layout
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Client for retrieving the user's current location
    private lateinit var locationRequest: LocationRequest // Request object for location updates
    private lateinit var locationCallback: LocationCallback // Callback used for receiving location updates
    private lateinit var placesClient: PlacesClient // Client for interacting with Places API
    private lateinit var midpoint: LatLng // Midpoint passed as an intent extra from StartNewFika activity

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialises the Places API with the provided API key and creates a PlacesClient instance.
        Places.initialize(applicationContext, "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs")
        placesClient = Places.createClient(this)

        // Inflates the layout file using the generated binding class.
        binding = ActivityNewFikaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the midpoint from the intent extras
        midpoint = intent.getParcelableExtra("midpoint") ?: LatLng(0.0, 0.0)

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@NewFika)
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
                    val userLocation = LatLng(location.latitude, location.longitude)
                    val markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)
                    val markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("User's Location")
                        .snippet("Additional information about the location")
                        .icon(markerIcon)

                    // Adds marker to map
                    mMap.addMarker(markerOptions)
                    // Moves camera to marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                }
            }
        }

//        searchButton.setOnClickListener {
//            GlobalScope.launch(Dispatchers.Main) {
//                val apiKey = "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs"
//                val firstLocation =
//                    convertAddressToCoordinates(editTextLocationA.text.toString(), apiKey)
//                val secondLocation =
//                    convertAddressToCoordinates(editTextLocationB.text.toString(), apiKey)
//
//                if (firstLocation != null && secondLocation != null) {
//                    // Calculate the midpoint using the coordinates
//                    val midpoint = calculateMidpoint(firstLocation, secondLocation)
//
//                    // Display the midpoint on the map and find nearby cafes
//                    showMidpointOnMap(midpoint)
//                    // findNearbyCafes(midpoint)
//                    findNearbyCafes(midpoint)
//                } else {
//                    // Handle geocoding failure for either firstLocation or secondLocation
//                }
//            }
//        }
    }

    private fun findNearbyCafes(midpoint: LatLng) {
        // Url to make the request to Google Places API for nearby cafes
        val url =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${midpoint.latitude},${midpoint.longitude}&radius=1000&type=cafe&key=AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs"

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
                val cafes = parseCafes(responseBody)
                runOnUiThread {
                    showCafes(cafes)
                }
            }

            /**
             * Handles the failure of the HTTP request to fetch cafes.
             *
             * @param call The Call object representing the HTTP request.
             * @param e The IOException indicating the cause of the failure.
             */
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@NewFika, "Failed to fetch cafes", Toast.LENGTH_SHORT).show()
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

            // Iterates over the resultsArray and extracts values from the JSON objects and create Cafe instances with them.
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
            val cafeLatLng = LatLng(cafe.latitude, cafe.longitude)

            val markerOptions = MarkerOptions()
                .position(cafeLatLng)
                .title(cafe.name)

            mMap.addMarker(markerOptions)
        }
    }

    private fun showMidpointOnMap(midpoint: LatLng) {

        // Clear existing markers on the map
        mMap.clear()

        // Create a custom marker icon with black color
        val markerIcon =
            BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)

        // Add a marker for the midpoint
        val markerOptions = MarkerOptions()
            .position(midpoint)
            .title("Midpoint")
            .snippet("This is the calculated midpoint")
            .icon(markerIcon)

        mMap.addMarker(markerOptions)

        // Move the camera to the midpoint
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15f))

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
                val intent = Intent(this@NewFika, StartNewFika::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    private suspend fun convertAddressToCoordinates(userAddress: String, apiKey: String): LatLng? {
//        val client = OkHttpClient()
//        val url =
//            "https://maps.googleapis.com/maps/api/geocode/json?address=$userAddress&key=$apiKey"
//
//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        val response = withContext(Dispatchers.IO) {
//            client.newCall(request).execute()
//        }
//
//        val responseData = response.body?.string()
//        response.close()
//
//        if (responseData != null) {
//            val jsonObject = JSONObject(responseData)
//            val results = jsonObject.getJSONArray("results")
//
//            if (results.length() > 0) {
//                val location =
//                    results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
//                val latitude = location.getDouble("lat")
//                val longitude = location.getDouble("lng")
//
//                return LatLng(latitude, longitude)
//            }
//        }
//
//        return null
//    }
//
//    private fun calculateMidpoint(firstLocation: LatLng, secondLocation: LatLng): LatLng {
//        val latMid = (firstLocation.latitude + firstLocation.latitude) / 2.0
//        val lngMid = (secondLocation.longitude + secondLocation.longitude) / 2.0
//        return LatLng(latMid, lngMid)
//    }

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
     * Callback method triggered when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the map instance.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Loads custom map style
        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light)
        googleMap.setMapStyle(mapStyleOptions)

        // Clear existing markers on the map
        mMap.clear()

        // Create a custom marker icon with black color
        val markerIcon =
            BitmapDescriptorFactory.fromResource(R.drawable.black_marker_icon)

        // Add a marker for the midpoint
        val markerOptions = MarkerOptions()
            .position(midpoint)
            .title("Midpoint")
            .snippet("This is the calculated midpoint")
            .icon(markerIcon)

        mMap.addMarker(markerOptions)

        // Move the camera to the midpoint
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15f))

        // Show nearby cafes
        findNearbyCafes(midpoint)
    }
}

