package com.example.fikabuild

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class WelcomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)

        //Button navigation
        loginButton.setOnClickListener {
            val intent = Intent(this@WelcomeScreen, LogIn::class.java)
            startActivity(intent)
        }

        signupButton.setOnClickListener {
            val intent = Intent(this@WelcomeScreen, SignUp::class.java)
            startActivity(intent)
        }
    }
}