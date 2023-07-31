package com.example.fikabuild

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

/**
 * Data class representing a cafe item.
 *
 * @property id The unique ID of the cafe.
 * @property name The name of the cafe.
 * @property address The address of the cafe.
 * @property imageUri The URI of the cafe image.
 * @property isFavorite Whether the cafe is marked as favorite or not.
 */
data class CafeData(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val imageUri: String? = null,
    var isFavorite: Boolean = false,
)

/**
 * RecyclerView adapter for displaying cafe items.
 *
 * @property cafeList The list of cafes to be displayed.
 * @property onFavoriteButtonClickListener Listener for favorite button clicks.
 */
class CafeAdapter(
    private val cafeList: List<CafeData>,
    private val onFavoriteButtonClickListener: (CafeData) -> Unit
) :
    RecyclerView.Adapter<CafeAdapter.CafeViewHolder>() {

    /**
     * ViewHolder class for the cafe item in the RecyclerView.
     *
     * @property itemView The view for the cafe item.
     */
    class CafeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        val firstImageButton: ImageButton = itemView.findViewById(R.id.firstImageButton)
    }

    /**
     * Creates a ViewHolder for each item in the RecyclerView.
     *
     * @param parent The parent view group.
     * @param viewType The type of the view.
     * @return The CafeViewHolder for the item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CafeViewHolder {
        // Inflate the layout for each item
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cafe, parent, false)
        return CafeViewHolder(itemView)
    }

    /**
     * Binds data to the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: CafeViewHolder, position: Int) {
        val cafe = cafeList[position]
        // Set the cafe name to the TextView
        holder.nameTextView.text = cafe.name
        // Set the cafe address to the TextView
        holder.addressTextView.text = cafe.address
        // Check if the imageUri is valid
        if (cafe.imageUri != null && cafe.imageUri.isNotEmpty()) {
            // Load the image using Glide if URI is valid
            loadRoundedImageWithGlide(holder.imageView, cafe.imageUri, holder.itemView.context)
        } else {
            // Set a placeholder image with rounded corners if URI is not valid
            Glide.with(holder.itemView)
                .load(R.drawable.rounded_placeholder)
                .apply(
                    RequestOptions.bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(
                                holder.itemView.context.resources.getDimensionPixelSize(
                                    R.dimen.rounded_corners
                                )
                            )
                        )
                    )
                )
                .into(holder.imageView)
        }

        // Set OnClickListener for the first image button to toggle the favourite state
        holder.firstImageButton.setOnClickListener {
            cafe.isFavorite = !cafe.isFavorite
            // Call the listener to handle the favorite button click action with the updated cafe object
            onFavoriteButtonClickListener(cafe)
        }
    }

    /**
     * Loads images with rounded corners using Glide and RoundedCornersTransformation.
     *
     * @param imageView The ImageView to load the image into.
     * @param imageUrl The URL of the image to load.
     * @param context The application context.
     */
    private fun loadRoundedImageWithGlide(
        imageView: ImageView,
        imageUrl: String,
        context: Context
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(
                RequestOptions.bitmapTransform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.rounded_corners))
                    )
                )
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    /**
     * Gets the number of items in the list.
     *
     * @return The number of items in the list.
     */
    override fun getItemCount(): Int {
        return cafeList.size
    }
}
