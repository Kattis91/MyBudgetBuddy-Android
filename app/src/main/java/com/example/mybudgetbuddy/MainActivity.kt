package com.example.mybudgetbuddy

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

import com.example.mybudgetbuddy.ui.theme.MyBudgetBuddyTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBudgetBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyBudgetBuddyNav()
                }
            }
        }
        askNotificationPermission()

        // Initialize Firebase Auth listener
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is signed in, refresh and save token
                getFCMTokenAndSaveToDatabase(currentUser.uid)
            }
        }
    }

    private fun getFCMTokenAndSaveToDatabase(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MyBudgetBuddy", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Save token to Firebase Database
            val database = FirebaseDatabase.getInstance()
            val reference = database.getReference("userTokens")

            reference.child(userId).setValue(mapOf("token" to token))
                .addOnSuccessListener {
                    Log.d("MyBudgetBuddy", "Token saved to database successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("MyBudgetBuddy", "Error saving token to database: ${e.message}")
                }

            Log.d("MyBudgetBuddy", "FCM Token: $token")
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("MyBudgetBuddy", "PERMISSION GRANTED")
        } else {
            Log.i("MyBudgetBuddy", "PERMISSION DENIED")
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("MyBudgetBuddy", "Permission ALREADY Granted")
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Log.i("MyBudgetBuddy", "Permission Show UI")
            } else {
                // Directly ask for the permission
                Log.i("MyBudgetBuddy", "Permission ASK USER")
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
