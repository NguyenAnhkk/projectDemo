package com.example.projectdemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.projectdemo.pages.LoginPage
import com.example.projectdemo.pages.MapUtils
import com.example.projectdemo.ui.theme.AuthViewModel
import com.example.projectdemo.ui.theme.MyAppNavigation
import com.example.projectdemo.ui.theme.ProjectDemoTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.rememberCameraPositionState
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val authViewModel: AuthViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {}
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        setContent {
            val context = LocalContext.current
            var currentLocation by remember {
                mutableStateOf(LatLng(21.0278, 105.8342))
            }
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation, 1f)
            }
            val mapUtils = MapUtils(context)
            ProjectDemoTheme {
                Scaffold{ innerPadding ->
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
