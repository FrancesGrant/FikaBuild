package com.example.fikabuild


import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

// Data class to store the cafe name and address
data class CafeData(val name: String, val address: String, val imageUri: String)
class SearchScreen : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient // Client for interacting with Places API
    private lateinit var cafeAdapter: CafeAdapter // Adapter for the RecyclerView
    private val cafeList: MutableList<CafeData> = mutableListOf() // List to store the cafe search results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_screen)

        // Initialize the Places API with the API key and create a PlacesClient instance
        Places.initialize(applicationContext, "AIzaSyAeDWvB01kaTU2ZpIm3qT2ueNbmiEYfDLs")
        placesClient = Places.createClient(this)

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text input
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        // Button
        val searchButton = findViewById<ImageButton>(R.id.searchUserButton)

        // Declare the RecyclerView
        val cafeRecyclerView = findViewById<RecyclerView>(R.id.cafeRecyclerView)
        cafeRecyclerView.layoutManager = LinearLayoutManager(this)

        // Create an instance of CafeAdapter and assign it to the cafeAdapter variable
        cafeAdapter = CafeAdapter(cafeList)
        cafeRecyclerView.adapter = cafeAdapter

        // Search button
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            searchCafes(query)
        }
    }

    private fun searchCafes(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                cafeList.clear() // Clear the previous results

                // Loop through the autocomplete predictions
                for (prediction in response.autocompletePredictions) {
                    val placeId = prediction.placeId
                    // Fetch the place details using the placeId
                    val placeRequest = FetchPlaceRequest.builder(
                        placeId,
                        listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS)
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

                                // Check if the photoUri is not null before loading the image with Glide
                                if (photoUri != null) {
                                    val cafeData = CafeData(name!!, address!!, photoUri)
                                    cafeList.add(cafeData)
                                    // Notify the adapter of the data change on the main thread
                                    cafeAdapter.notifyDataSetChanged()
                                } else {
                                    // Handle the case where photoUri is null (e.g., photo not available)
                                    Toast.makeText(
                                        this@SearchScreen,
                                        "Failed to load cafe photo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .addOnFailureListener {
                            // Handle place fetch failure
                            Toast.makeText(
                                this@SearchScreen,
                                "Failed to return cafe results",
                                Toast.LENGTH_SHORT
                            ).show()
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





    // Function to save Bitmap to a file
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } finally {
            outputStream?.close()
        }
    }


    // Adapter class for RecyclerView
    class CafeAdapter(private val cafeList: List<CafeData>) :
        RecyclerView.Adapter<CafeAdapter.CafeViewHolder>() {

        // ViewHolder class for the RecyclerView item
        class CafeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)

            // TextView to display the cafe name
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

            // TextView to display cafe address
            val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        }

        // Create ViewHolder for each item in the RecyclerView
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CafeViewHolder {
            // Inflate the layout for each item
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cafe, parent, false)
            return CafeViewHolder(itemView)
        }

        // Bind data to the ViewHolder
        override fun onBindViewHolder(holder: CafeViewHolder, position: Int) {
            val cafe = cafeList[position]
            // Set the cafe name to the TextView
            holder.nameTextView.text = cafe.name
            // Set the cafe address to the TextView
            holder.addressTextView.text = cafe.address

            // Check if the imageUri is valid before loading the image
            if (cafe.imageUri != null && cafe.imageUri.isNotEmpty()) {
                // Load the image using Glide and the content URI
                Glide.with(holder.itemView)
                    .load(Uri.parse(cafe.imageUri)) // Parse the content URI
                    .into(holder.imageView)
            } else {
                // Set a placeholder image
                Glide.with(holder.itemView)
                    .load(R.drawable.default_image) // Replace with your placeholder image resource
                    .into(holder.imageView)
            }
        }

        // Return the number of items in the list
        override fun getItemCount(): Int {
            return cafeList.size
        }
    }
}




