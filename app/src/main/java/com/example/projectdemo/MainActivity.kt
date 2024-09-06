
package com.example.projectdemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import com.example.projectdemo.pages.LoginPage
import com.example.projectdemo.pages.MapUtils
import com.example.projectdemo.ui.theme.AuthViewModel
import com.example.projectdemo.ui.theme.MyAppNavigation
import com.example.projectdemo.ui.theme.ProjectDemoTheme
import com.facebook.FacebookSdk
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.prefs.Preferences

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val authViewModel: AuthViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {}
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM","Fetching FCM registration token failed" , task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("FCM",token.toString())
            Toast.makeText(baseContext,token.toString(),Toast.LENGTH_SHORT).show()
        })
        setContent {
            ProjectDemoTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Column {
                            MyAppNavigation(
                                authViewModel = authViewModel,
                            )
                        }
                    }
                }
            }
        }
    }


}
