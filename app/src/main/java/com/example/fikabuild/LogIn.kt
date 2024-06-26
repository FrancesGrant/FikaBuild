package com.example.fikabuild

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/**
 * This activity allows users to log in to the application.
 */
class LogIn : AppCompatActivity() {

    lateinit var auth: FirebaseAuth // Firebase authentication instance
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent> // Launches the activity

    /**
     * Called when the activity is created or recreated.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        // Firebase authentication instance used for user authentication
        auth = Firebase.auth
        // Initialize the imageActivityResultLauncher here
        imageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result of the image activity here
        }
        // Toolbar for navigation
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Text inputs from user
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        // TextView
        val textViewForgotPassword = findViewById<TextView>(R.id.textViewForgotPassword)

        /**
         * Sets a click listener for Forgot Password
         * When clicked, it brings the user to the page to enter their credentials to get a login link
         */
        textViewForgotPassword.setOnClickListener{
            val intent = Intent(this@LogIn, ResetPassword::class.java)
            startActivity(intent)
        }

        /**
         * Sets a click listener for the Login button.
         * When clicked, it signs the user in if the email and password authenticates.
         * If the login is successful, the user receives a toast message.
         *
         */
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@LogIn, MapsActivity::class.java)
                        imageActivityResultLauncher.launch(intent)
                    } else {
                        Toast.makeText(this@LogIn, "Login unsuccessful", Toast.LENGTH_SHORT).show()
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
                val intent = Intent(this@LogIn, WelcomeScreen::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}





