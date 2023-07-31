package com.example.fikabuild

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SignUpTest {
    @Test
    fun testSignUpButton_Failure_ShowToast() {
        // Start the SignUp activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), SignUp::class.java)
        val scenario = ActivityScenario.launch<LogIn>(intent)

        // Find views by their IDs
        var emailEditText: EditText
        var passwordEditText: EditText
        var signUpButton: Button
    }
}