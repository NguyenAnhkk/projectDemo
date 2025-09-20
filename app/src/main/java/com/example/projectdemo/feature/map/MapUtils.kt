package com.example.projectdemo.feature.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.example.projectdemo.feature.profile.loadImageUrlFromFirestore
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

    fun updateUserOnlineStatus() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = Firebase.auth.currentUser?.uid ?: return
        firestore.collection("profile").document(userId)
            .update("lastSeen", System.currentTimeMillis())
    }

    private fun startLocationUpdates(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        onLocationFetched: (LatLng) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
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

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun LocationScreen(
        camerapositionState: CameraPositionState,
        context: Context,
        navController: NavController,
        currentLocation: LatLng?,
    ) {
        var nearbyUsersLocations by remember { mutableStateOf(emptyList<LatLng>()) }
        var radiusInMeters by remember { mutableStateOf(5000f) }
        var currentLocation by rememberSaveable { mutableStateOf<LatLng?>(null) }
        var isLocationUpdated by remember { mutableStateOf(false) }
        val bitmap = BitmapFactory.decodeResource(context.resources, R.raw.markermap)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val smallIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val userPhotoUrl = Firebase.auth.currentUser?.photoUrl?.toString()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val launchMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            if (permissionsMap.all { it.value }) {

                startLocationUpdates(fusedLocationClient, context) { location ->
                    currentLocation = location
                    isLocationUpdated = true
                }
            } else {

                Toast.makeText(context, "Cần cấp quyền vị trí để tiếp tục", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        var currentAddress by remember { mutableStateOf("Update location") }
        fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            return if (addresses.isNullOrEmpty()) {
                "Không xác định"
            } else {

                val addressLine = addresses[0].getAddressLine(0)
                val addressParts = addressLine.split(",")
                if (addressParts.isNotEmpty()) {
                    addressParts[0] + "," + addressParts[1]
                } else {
                    "Không xác định"
                }
            }
        }

        LaunchedEffect(currentLocation) {
            currentLocation?.let {
                currentAddress = getAddressFromLatLng(context, it)
                updateMyLocationInFirestore(it)
            }
        }

        LaunchedEffect(currentLocation, radiusInMeters) {
            currentLocation?.let { safeCurrentLocation ->
                fetchNearbyUsersLocations { locations ->
                    nearbyUsersLocations = locations.filter { location ->
                        isWithinRadius(safeCurrentLocation, location, radiusInMeters)
                    }
                }
            }
        }

        Scaffold() {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camerapositionState
                ) {
                    if (isLocationUpdated && currentLocation != null) {
                        Marker(
                            state = MarkerState(position = currentLocation!!),
                            title = "Vị trí của bạn",
                            snippet = "Địa chỉ hiện tại",
                            icon = smallIcon,
                        )
                    }

                    nearbyUsersLocations.forEach { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Người dùng xung quanh",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }


                LaunchedEffect(isLocationUpdated) {
                    if (isLocationUpdated && currentLocation != null) {
                        camerapositionState.animate(CameraUpdateFactory.newLatLng(currentLocation!!))
                    }
                }


                nearbyUsersLocations.forEach { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Người dùng xung quanh",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }

                LaunchedEffect(currentLocation) {
                    currentLocation?.let {
                        updateMyLocationInFirestore(it)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
                        CustomTopBar(
                            currentAddress = currentAddress,
                            userPhotoUrl = userPhotoUrl,
                            navController = navController,
                        )
                    }

                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = {
                        if (permissions.all {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    it
                                ) == PackageManager.PERMISSION_GRANTED
                            }) {
                            // Kiểm tra xem GPS có bật không
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                            if (isGpsEnabled) {
                                startLocationUpdates(fusedLocationClient, context) { location ->
                                    currentLocation = location
                                    isLocationUpdated = true
                                }
                            } else {
                                // Yêu cầu bật GPS
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            }
                        } else {
                            launchMultiplePermissions.launch(permissions)
                        }
                    }
                    , shape = RoundedCornerShape(16.dp) , colors =  ButtonDefaults.buttonColors(Color(0xFFb631eb)) , modifier =  Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text(text = "Cập nhật vị trí.")
                    }
                }
            }
        }
    }

    @Composable
    fun IconButtonWithImage(navController: NavController, userPhotoUrl: String?) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        loadImageUrlFromFirestore({ url ->
            imageUrl = url
        }, {
            Toast.makeText(context, "Không thể tải ảnh từ Firestore", Toast.LENGTH_SHORT).show()
        })
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = { navController.navigate("profile") },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    when {
                        imageUrl != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrl),
                                contentDescription = "User Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.defaultimg),
                                contentDescription = "Default Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),

                                )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CustomTopBar(
        currentAddress: String,
        userPhotoUrl: String?,
        navController: NavController,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Text(
                text = currentAddress,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(4.dp, 0.dp, 0.dp, 0.dp)
            )
            IconButtonWithImage(navController = navController, userPhotoUrl = userPhotoUrl)
        }

    }

}
