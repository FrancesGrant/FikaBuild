package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity that allows users to review their account settings.
 */
class ProfileScreen : AppCompatActivity() {

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_screen)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)

        // TextView
        val textViewForgotPassword = findViewById<TextView>(R.id.textViewForgotPassword)

        // Buttons
        val updateButton = findViewById<Button>(R.id.updateButton)

        /**
         * Sets a click listener for Forgot Password
         * When clicked, it brings the user to the page to enter their credentials to get a login link
         */
        textViewForgotPassword.setOnClickListener{
            val intent = Intent(this@ProfileScreen, ResetPassword::class.java)
            startActivity(intent)
        }

        /**
         * Sets a click listener for update button
         * When clicked, it updates the users credentials with their input
         */
        updateButton.setOnClickListener {
            val newEmail = editTextEmail.text.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.updateEmail(newEmail)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email update successful
                        Toast.makeText(this@ProfileScreen, "Email updated successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Email update failed
                        val errorMessage = task.exception?.message ?: "Email update failed."
                        Toast.makeText(this@ProfileScreen, errorMessage, Toast.LENGTH_SHORT).show()
                    }
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
                val intent = Intent(this@ProfileScreen, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
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
}