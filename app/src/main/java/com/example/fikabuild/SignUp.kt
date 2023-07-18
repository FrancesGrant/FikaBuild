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
 * This activity allows users to sign up to the application.
 *
 * The Firebase Authentication instance is used for user authentication.
 */

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Firebase authentication instance used for user authentication
        auth = Firebase.auth
        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Text inputs from user
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        // Buttons
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        /**
         * Sets an OnClickListener on the signUpButton to handle the sign up process.
         * When clicked, the user is signed up and the main activity is launched.
         * If unsuccessful, the user receives a toast message.
         */
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Create a new user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // If the sign-up is successful the main activity is launched
                        val intent = Intent(this@SignUp, MapsActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If the sign-up failed the user receives an error message
                        Toast.makeText(this@SignUp, "Sign up has failed please try again", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Overrides the `onOptionsItemSelected` method to handle menu item selections.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@SignUp, WelcomeScreen::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}