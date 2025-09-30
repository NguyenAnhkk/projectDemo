
package com.example.projectdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.projectdemo.lib.MyAppTheme
import com.example.projectdemo.feature.auth.common.AuthViewModel
import com.example.projectdemo.feature.map.ManagerLocation
import com.example.projectdemo.ui.theme.MyAppNavigation
import com.example.projectdemo.ui.theme.ProjectDemoTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val authViewModel: AuthViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        ManagerLocation.initialize(this)
        setContent {
            ProjectDemoTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MyAppTheme {
                            MyAppNavigation(authViewModel = authViewModel)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            MapsInitializer.initialize(this@MainActivity, MapsInitializer.Renderer.LATEST) {}
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

            val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", token.toString())
                    sharedPreferences.edit().putString("fcm_token", token).apply()
                } else {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                }
            }
        }
    }
}
