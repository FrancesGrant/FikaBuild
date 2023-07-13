package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

/**
 * This activity allows the user to select if the want to log in or sign up to the application.
 *
 */
class WelcomeScreen : AppCompatActivity() {

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        // Buttons that allow the user to log in or sign up.
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)

        /**
         * Sets an OnClickListener on the log in button to start the log in process.
         */
        loginButton.setOnClickListener {
            val intent = Intent(this@WelcomeScreen, LogIn::class.java)
            startActivity(intent)
        }

        /**
         * Sets an OnClickListener on the sign up button to start the sign up process.
         */
        signupButton.setOnClickListener {
            val intent = Intent(this@WelcomeScreen, SignUp::class.java)
            startActivity(intent)
        }
    }
}