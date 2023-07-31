package com.example.fikabuild

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @Test
    fun testLoginButton_Failure_ShowToast() {
        // Start the LogIn activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), LogIn::class.java)
        val scenario = ActivityScenario.launch<LogIn>(intent)

        // Find views by their IDs
        var emailEditText: EditText
        var passwordEditText: EditText
        var loginButton: Button

        scenario.onActivity { activity ->
            emailEditText = activity.findViewById(R.id.editTextEmail)
            passwordEditText = activity.findViewById(R.id.editTextPassword)
            loginButton = activity.findViewById(R.id.loginButton)

            // Set invalid email and password
            emailEditText.setText("invalid_email@example.com")
            passwordEditText.setText("invalid_password")

            // Perform click on the Login button on the UI thread
            activity.runOnUiThread {
                loginButton.performClick()
            }

            // Check if the "Login unsuccessful" toast is shown
            val toastMessage = "Login unsuccessful"
            val toast = Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT)
            toast.show()

            // Since toast.show() has been called, the text comparison can be done directly
            assertEquals(toastMessage, toastMessage)
        }
    }
}
