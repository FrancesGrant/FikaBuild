package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity that allows users to request a password reset link to be sent to their email address.
 */
class ResetPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth // Firebase authentication instance
    private lateinit var firebaseAuth: FirebaseAuth // Firebase authentication instance

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        firebaseAuth = FirebaseAuth.getInstance()

        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Text inputs
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)

        // Buttons
        val resetButton = findViewById<Button>(R.id.resetButton)

        // Firebase authentication instance used for user authentication
        auth = Firebase.auth

        /**
         * Sets a click listener for the Reset button.
         * When clicked, it sends a password reset email to the user's provided email address.
         */
        resetButton.setOnClickListener{
            val email = editTextEmail.text.toString()

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        Toast.makeText(this@ResetPassword, "Password reset email sent, check your inbox.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ResetPassword, "Error occurred, please check if email is correct", Toast.LENGTH_SHORT).show()
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
                val intent = Intent(this@ResetPassword, LogIn::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}