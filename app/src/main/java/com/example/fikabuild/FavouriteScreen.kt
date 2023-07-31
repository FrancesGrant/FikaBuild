package com.example.fikabuild
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity that displays the user's favorite cafes.
 * Users can view and manage their favorite cafes in this screen.
 */
class FavouriteScreen : AppCompatActivity() {
    private lateinit var cafeAdapter: CafeAdapter // Adapter for the RecyclerView displaying favorite cafes
    private val cafeList: MutableList<CafeData> = mutableListOf() // List of favorite cafes
    private lateinit var favoritesRecyclerView: RecyclerView // RecyclerView for displaying favorite cafes
    private lateinit var auth: FirebaseAuth // Firebase authentication instance
    private lateinit var firestore: FirebaseFirestore // Firestore database instance

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_screen)

        // Initialize FirebaseAuth and FirebaseFirestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Declare the RecyclerView
        favoritesRecyclerView = findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Cafe adapter for RecyclerView
        cafeAdapter = CafeAdapter(cafeList) { cafe ->
            /**
             * Handles the favorite button click action for a cafe item in the RecyclerView.
             *
             * @param cafe The [CafeData] object representing the cafe item that was clicked.
             * @see removeFromFavorites
             */
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                if (!cafe.isFavorite) { // Check if the cafe is in favorites before trying to remove
                    removeFromFavorites(userUid, cafe)
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Sets up the RecyclerView with the CafeAdapter and fetches the user's favorite cafes from Firestore.
         * The user's favorite cafes are displayed in the RecyclerView on the activity start.
         *
         * @see CafeAdapter
         * @see fetchFavorites
         */
        favoritesRecyclerView.adapter = cafeAdapter
        // Fetch the user's favorite cafes from Firestore when the activity starts
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            fetchFavorites(userUid)
        }
    }

    /**
     * Fetches the user's favorite cafes from Firestore and updates the RecyclerView with the retrieved data.
     *
     * @param userUid The user's unique ID used to identify the user in Firestore.
     */
    private fun fetchFavorites(userUid: String) {
        // Reference to the "favorites" collection for the user in Firestore
        val favoritesRef = firestore.collection("users").document(userUid).collection("favorites")
        favoritesRef.get()
            .addOnSuccessListener { querySnapshot ->
                val newFavoritesCafeList = mutableListOf<CafeData>()
                for (document in querySnapshot.documents) {
                    // Convert each document to CafeData and add it to the list
                    val cafe = document.toObject(CafeData::class.java)
                    cafe?.let {
                        newFavoritesCafeList.add(it)
                    }
                }
                cafeList.clear()
                cafeList.addAll(newFavoritesCafeList)
                cafeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Handle the error while fetching favorites
                Toast.makeText(this, "Failed to fetch favorites", Toast.LENGTH_SHORT).show()
            }
    }


    /**
     * Removes the specified cafe from the user's favorites in Firestore.
     *
     * @param userUid The user's unique ID used to identify the user in Firestore.
     * @param cafe The cafe data to be added to the favorites.
     */
    private fun removeFromFavorites(userUid: String, cafe: CafeData) {
        val favoritesRef = firestore.collection("users").document(userUid).collection("favorites")
        val cafeId = cafe.id // Assuming cafe ID is a unique identifier for the cafe
        if (cafeId != null) {
            favoritesRef.document(cafeId)
                .delete()
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
                val intent = Intent(this@FavouriteScreen, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
