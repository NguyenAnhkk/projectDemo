package com.example.projectdemo.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projectdemo.R
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MapUtils(private val context: Context) {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    private var locationRequired: Boolean = false

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        setupLocationCallback()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                if (locationRequired) {
                    // Handle location update
                }
            }
        }
    }

    private fun startLocationUpdates(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        onLocationFetched: (LatLng) -> Unit
    ) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 seconds
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    Toast.makeText(
                        context,
                        "Location updated: ${location.latitude}, ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onLocationFetched(latLng)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)

    }

    @Composable
    fun LocationScreen(
        camerapositionState: CameraPositionState,
        context: Context,
        currentLocation: LatLng,
    ) {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.raw.markermap)
        val newWidth = 100
        val newHeight = 100
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val smallIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        val currentContext = LocalContext.current
        var currentLocation by remember { mutableStateOf(LatLng(21.0278, 105.8342)) }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(currentContext)

        val launchMultiplePermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            if (permissionsResult.all { it.value }) {
                startLocationUpdates(fusedLocationClient, currentContext) { location ->
                    currentLocation = location
                }
            } else {
                Toast.makeText(currentContext, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }


        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camerapositionState
            ) {
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Nè !",
                    snippet = "Ngọc Anh đang ở đây!!!",
                    icon = smallIcon,
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Your location: ${currentLocation.latitude}/${currentLocation.longitude}")
                Button(onClick = {
                    if (permissions.all {
                            ContextCompat.checkSelfPermission(
                                currentContext,
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        fetchLocation(fusedLocationClient, currentContext) { location ->
                            currentLocation = location
                        }
                    } else {
                        launchMultiplePermissions.launch(permissions)
                    }
                }) {
                    Text(text = "Get your location")
                }

            }
        }
    }

    private fun fetchLocation(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        onLocationFetched: (LatLng) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                Toast.makeText(
                    context,
                    "${location.latitude}, ${location.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
                onLocationFetched(latLng)
            }
        }
    }
}
