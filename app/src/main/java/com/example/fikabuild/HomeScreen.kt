package com.example.fikabuild

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * This activity allows users to access the main dashboard in the application
 *
 */
class HomeScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    /**
     * Overrides the `onCreate` method of the activity to initialize and set up the login screen.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.customActionBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = Firebase.auth
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

    /**
     * Overrides the `onOptionsItemSelected` method of the activity to handle menu item selection.
     *
     * @param item The selected menu item.
     * @return Boolean value indicating whether the item selection is handled.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "Logout") {
            Firebase.auth.signOut()
            navigateToLogin()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Navigates the user to Login Activity.
     *
     */
    private fun navigateToLogin() {
        val intent = Intent(this, WelcomeScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}


