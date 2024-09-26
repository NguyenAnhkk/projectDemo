package com.example.projectdemo.pages.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectdemo.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import java.util.Locale

class MapUtils(private val context: Context) {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
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
                // Handle location update
            }
        }
    }

    fun fetchNearbyUsersLocations(onLocationsFetched: (List<LatLng>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Courses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val locations = snapshot.documents.mapNotNull { doc ->
                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")
                    if (lat != null && lng != null) LatLng(lat, lng) else null
                }
                onLocationsFetched(locations)
            }
    }

    fun isWithinRadius(
        currentLocation: LatLng,
        userLocation: LatLng,
        radiusInMeters: Float
    ): Boolean {
        val currentLoc = Location("").apply {
            latitude = currentLocation.latitude
            longitude = currentLocation.longitude
        }
        val userLoc = Location("").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        return currentLoc.distanceTo(userLoc) <= radiusInMeters
    }

    private fun updateMyLocationInFirestore(latLng: LatLng) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = Firebase.auth.currentUser?.uid ?: return
        firestore.collection("location").document(userId)
            .set(mapOf("latitude" to latLng.latitude, "longitude" to latLng.longitude))
    }

    private fun startLocationUpdates(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        onLocationFetched: (LatLng) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L // 10 seconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 seconds
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
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
        ) return

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun LocationScreen(
        camerapositionState: CameraPositionState,
        context: Context,
        navController: NavController,
        currentLocation: LatLng,
    ) {
        var nearbyUsersLocations by remember { mutableStateOf(emptyList<LatLng>()) }
        var radiusInMeters by rememberSaveable { mutableStateOf(5000f) } // Default 5km radius
        var currentLocation by rememberSaveable { mutableStateOf(LatLng(21.0176342, 105.837429)) }
        val bitmap = BitmapFactory.decodeResource(context.resources, R.raw.markermap)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val smallIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val userPhotoUrl = Firebase.auth.currentUser?.photoUrl?.toString()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        LaunchedEffect(currentLocation, radiusInMeters) {
            fetchNearbyUsersLocations { locations ->
                nearbyUsersLocations = locations.filter { location ->
                    isWithinRadius(currentLocation, location, radiusInMeters)
                }
            }
        }

        val launchMultiplePermissions = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            if (permissionsResult.all { it.value }) {
                startLocationUpdates(fusedLocationClient, context) { location ->
                    currentLocation = location
                }
            } else {
                Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

        Scaffold(topBar = {
            IconButtonWithImage(navController = navController, userPhotoUrl = userPhotoUrl)
        }) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camerapositionState
                ) {
                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Vị trí của bạn",
                        snippet = "Địa chỉ hiện tại",
                        icon = smallIcon,
                    )
                }

                LaunchedEffect(currentLocation) {
                    camerapositionState.animate(CameraUpdateFactory.newLatLng(currentLocation))
                }

                nearbyUsersLocations.forEach { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Người dùng xung quanh",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                LaunchedEffect(currentLocation) {
                    updateMyLocationInFirestore(currentLocation)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Slider to change radius dynamically
                    Slider(
                        value = radiusInMeters,
                        onValueChange = { newRadius ->
                            radiusInMeters = newRadius
                        },
                        valueRange = 1000f..10000f,
                        steps = 8,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(text = "Radius: ${radiusInMeters.toInt()} meters", fontWeight = FontWeight.Bold)

                    Button(onClick = {
                        if (permissions.all {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    it
                                ) == PackageManager.PERMISSION_GRANTED
                            }) {
                            startLocationUpdates(fusedLocationClient, context) { location ->
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
    }

    @Composable
    fun IconButtonWithImage(navController: NavController, userPhotoUrl: String?) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .shadow(2.dp, CircleShape)
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.person),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
