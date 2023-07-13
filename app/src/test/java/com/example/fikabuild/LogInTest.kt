package com.example.fikabuild

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LogInTest {
    @Mock
    private lateinit var mockAuth: FirebaseAuth
    @Mock
    private lateinit var mockActivityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var logInActivity: LogIn

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        logInActivity = LogIn()
        logInActivity.auth = mockAuth
        logInActivity.imageActivityResultLauncher = mockActivityResultLauncher

    }

    /**
     * Test verifies that when a sign-in is successful, the appropriate intent is launched to navigate to the MapsActivity class.
     * Test uses a stubbed behaviour for the signInWithEmailAndPassword method.
     * When the task is completed it verifies if the sign-in was successful and launches the MapsActivity.
     * If the test fails and the sign-in is not successful an error message is displayed.
     */
    @Test
    fun testOnCreate_SuccessfulSignIn() {
        val email = "test@example.com"
        val password = "password123"

        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenAnswer {
            val task = mock(Task::class.java) as Task<AuthResult>
            `when`(task.isSuccessful).thenReturn(true)
            task.addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val intentCaptor = ArgumentCaptor.forClass(Intent::class.java)
                    verify(mockActivityResultLauncher).launch(intentCaptor.capture())
                    val intent = intentCaptor.value
                    assertEquals(MapsActivity::class.java, intent.component?.className)
                } else {
                    fail("Sign-in not successful")
                }
            }
        }
    }
}
