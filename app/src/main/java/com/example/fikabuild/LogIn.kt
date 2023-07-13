package com.example.fikabuild

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * This activity allows users to log in to the application.
 */

class LogIn : AppCompatActivity() {

    // Class-level variables
    lateinit var auth: FirebaseAuth
    lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

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
        // Button to login
        val loginButton = findViewById<Button>(R.id.loginButton)

        /**
         * Sets an OnClickListener on the loginButton to handle the login process.
         */
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val intent = Intent(this@LogIn, HomeScreen::class.java)
                        imageActivityResultLauncher.launch(intent)
                    } else {
                        // Sign-up failed, handle the error
                        Toast.makeText(this@LogIn, "Login unsuccessful", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Overrides the `onOptionsItemSelected` method to handle the functionality of the Toolbar back button.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@LogIn, WelcomeScreen::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}





