package com.example.fikabuild

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Activity that allows the users to search for cafes to add to their favourites
 */
class SearchScreen : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient // Client for interacting with the Places API
    private lateinit var cafeAdapter: CafeAdapter // Adapter for the RecyclerView
    private val cafeList: MutableList<CafeData> = mutableListOf() // List of cafe's displayed in Recyclerview
    private var userUid: String? = null // User ID for the current user
    private lateinit var firestore: FirebaseFirestore // Firebase instance for database interaction
    private lateinit var auth: FirebaseAuth // Firebase authentication for user authentication

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_screen)

        // Initialise FirebaseAuth, FirebaseFirestore, and userUid
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        getUserUid()

        // Initialise Google Places API
        Places.initialize(applicationContext, "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs")
        placesClient = Places.createClient(this)

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs from user
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        // Buttons
        val searchButton = findViewById<ImageButton>(R.id.searchUserButton)

        /**
         * Initializes the [cafeAdapter] for the [RecyclerView].
         *
         * The [CafeAdapter] is responsible for displaying the list of cafes in the [RecyclerView] and handling the
         * favorite button click action.
         *
         * @param cafeList The list of cafes to be displayed in the [RecyclerView].
         * @param onFavoriteButtonClickListener The callback to handle the favorite button click action.
         */
        cafeAdapter = CafeAdapter(cafeList) { cafe ->
            // Handle the favorite button click action to add or remove from favourites
            if (cafe.isFavorite) {
                addToFavorites(cafe)
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                removeFromFavorites(cafe)
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
        }

        // RecyclerViewer to display cafe items
        val cafeRecyclerView = findViewById<RecyclerView>(R.id.cafeRecyclerView)
        cafeRecyclerView.layoutManager = LinearLayoutManager(this)
        cafeRecyclerView.adapter = cafeAdapter

        /**
         * Sets a click listener for searchButton.
         * When clicked, searchCafe function is called with the text input from the user as the query.
         */
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            searchCafes(query)
        }
    }

        /**
        * Fetches the current user's unique identifier (UID) from Firebase Authentication.
        */
        private fun getUserUid() {
            val currentUser = FirebaseAuth.getInstance().currentUser
            userUid = currentUser?.uid
        }

    /**
     * Searches for cafes based on the provided query using the Places API Autocomplete feature.
     * The [query] is used to find autocomplete predictions for cafe names or addresses.
     * The search results are fetched and displayed in the RecyclerView.
     *
     * @param query The search query string to find cafes.
     */
        private fun searchCafes(query: String) {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    // Create a new list to store the search results
                    val newCafeList = mutableListOf<CafeData>()
                    // Create a counter to keep track of the number of cafe data fetched
                    var cafesFetched = 0
                    // Loop through the autocomplete predictions
                    for (prediction in response.autocompletePredictions) {
                        val placeId = prediction.placeId
                        // Fetch the place details using the placeId
                        val placeRequest = FetchPlaceRequest.builder(
                            placeId,
                            listOf(
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.PHOTO_METADATAS
                            )
                        )
                            .build()
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { fetchPlaceResponse ->
                                val place = fetchPlaceResponse.place
                                val name = place.name
                                val address = place.address
                                val photoMetadata = place.photoMetadatas?.get(0)
                                // Use lifecycleScope.launch to run the suspend function in a coroutine scope
                                lifecycleScope.launch {
                                    val photoUri = photoMetadata?.let { getPhotoUri(it) }
                                    // Check if both name and address are not null before creating the CafeData object
                                    if (name != null && address != null && photoUri != null) {
                                        val cafeData = CafeData(
                                            id = placeId,
                                            name = name,
                                            address = address,
                                            imageUri = photoUri,
                                        )
                                        // Add the cafeData to the new list
                                        newCafeList.add(cafeData)
                                    } else {
                                        // Handle the case where either name, address, or photoUri is null
                                        Toast.makeText(
                                            this@SearchScreen,
                                            "Failed to load cafe details",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    cafesFetched++
                                    // Check if all cafes have been fetched before notifying the adapter
                                    if (cafesFetched == response.autocompletePredictions.size) {
                                        cafeList.clear()
                                        cafeList.addAll(newCafeList)
                                        cafeAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                            .addOnFailureListener {
                                // Handle place fetch failure
                                cafesFetched++
                                // Check if all cafes have been fetched before notifying the adapter
                                if (cafesFetched == response.autocompletePredictions.size) {
                                    cafeList.clear()
                                    cafeList.addAll(newCafeList)
                                    cafeAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    // Handle autocomplete prediction fetch failure
                    Toast.makeText(
                        this@SearchScreen,
                        "Failed to predict cafe results",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }


    /**
     * Fetches the content URI of a photo using [PlacesClient]
     *
     * @param photoMetadata The [PhotoMetadata] object contains information about the photo to be fetched.
     * @return The content URI of the fetched photo as a [String] or 'null'.
     * @throws IOException If there is an error saving the Bitmap file.
     *
     */
    private suspend fun getPhotoUri(photoMetadata: PhotoMetadata): String? {
            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(500) // Set the maximum width of the image here if needed
                .setMaxHeight(500) // Set the maximum height of the image here if needed
                .build()
            val photoResponse = placesClient.fetchPhoto(photoRequest).await()
            // Check if the photoResponse contains a Bitmap
            if (photoResponse.bitmap == null) {
                return null // Return null if the response doesn't contain a Bitmap
            }
            // Save the Bitmap to a file and get its URI using FileProvider
            val photoBitmap: Bitmap = photoResponse.bitmap
            // Create a new file in the external cache directory
            val photoFile = File(
                applicationContext.cacheDir, // Use cache directory instead of external storage
                "place_photo_${System.currentTimeMillis()}.jpg"
            )
            // Save the Bitmap to the file
            withContext(Dispatchers.IO) {
                saveBitmapToFile(photoBitmap, photoFile)
            }
            // Get the content URI for the saved photo file using FileProvider
            val contentUri = FileProvider.getUriForFile(
                applicationContext,
                "com.example.fikabuild.fileprovider", // Update with your authority from the manifest
                photoFile
            )

            return contentUri?.toString()
        }

        /**
        * Saves the [Bitmap] to the specified [File].
         * @param bitmap The [Bitmap] to be saved.
         * @param file The [File] to which the [Bitmap] will be saved.
         * @throws IOException If there is an error saving the [Bitmap] to the [File].
        */
        private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } finally {
                outputStream?.close()
            }
        }

    /**
     * Adds the specified [cafe] to the user's favorites in Firestore.
     *
     * @param cafe The [CafeData] object representing the cafe to be added to favorites.
     */
    private fun addToFavorites(cafe: CafeData) {
        userUid?.let { userUid ->
            // Reference to the "favorites" collection for the user in Firestore
            val favoritesRef = firestore.collection("users").document(userUid)
                .collection("favorites").document(cafe.id)
            // Create a map containing cafe data to be stored in Firestore
            val cafeDataMap = mapOf(
                "id" to cafe.id,
                "name" to cafe.name,
                "address" to cafe.address,
                "imageUri" to cafe.imageUri,
                "isFavorite" to cafe.isFavorite // Include isFavorite property
            )
            favoritesRef.set(cafeDataMap)
                .addOnSuccessListener {
                    // Success: Cafe added to favorites
                }
                .addOnFailureListener {
                    // Handle the error
                    Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                }
        }
    }


    /**
     * Removes the specified [cafe] from the user's favorites in Firestore.
     *
     * @param cafe The [CafeData] object representing the cafe to be removed from favorites.
     */
    private fun removeFromFavorites(cafe: CafeData) {
        userUid?.let { userUid ->
            // Reference to the "favorites" collection for the user in Firestore
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("users").document(userUid)
                .collection("favorites").document(cafe.id)

            // Delete the document
            favoritesRef.delete()
                .addOnSuccessListener {
                    // Success: Cafe removed from favorites
                }
                .addOnFailureListener {
                    // Handle the error
                    Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                }
        }
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
                    val intent = Intent(this@SearchScreen, MapsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

