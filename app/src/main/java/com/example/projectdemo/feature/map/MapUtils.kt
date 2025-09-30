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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    fun getAddressFromLatLng(latLng: LatLng): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                "Unknown"
            } else {
                val addressLine = addresses[0].getAddressLine(0)
                val addressParts = addressLine.split(",")
                if (addressParts.size >= 2) {
                    addressParts[0] + "," + addressParts[1]
                } else {
                    addressLine
                }
            }
        } catch (e: Exception) {
            "Unable to determine address"
        }
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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(latLng)

                    ManagerLocation.updateLocation(latLng, context)
                    ManagerLocation.currentAddress = getAddressFromLatLng(latLng)
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
        navController: NavController,
    ) {
        var isLoading by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var searchQuery by remember { mutableStateOf("") }
        var searchError by remember { mutableStateOf<String?>(null) }
        val focusManager = LocalFocusManager.current
        var nearbyUsersLocations by remember { mutableStateOf(emptyList<LatLng>()) }
        var radiusInMeters by remember { mutableStateOf(5000f) }
        var currentLocation by rememberSaveable { mutableStateOf<LatLng?>(null) }
        var isLocationUpdated by remember { mutableStateOf(false) }
        val bitmap = BitmapFactory.decodeResource(context.resources, R.raw.markermap)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val smallIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val userPhotoUrl = Firebase.auth.currentUser?.photoUrl?.toString()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val keyboardController = LocalSoftwareKeyboardController.current
        val launchMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            if (permissionsMap.all { it.value }) {
                startLocationUpdates(fusedLocationClient, context) { location ->
                    currentLocation = location
                    isLocationUpdated = true
                }
            } else {
                Toast.makeText(
                    context,
                    "Location permission required to continue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        var currentAddress by remember { mutableStateOf("Update location") }

        fun searchLocation(
            query: String,
            camerapositionState: CameraPositionState,
            coroutineScope: CoroutineScope,
            onSuccess: (LatLng) -> Unit,
            onError: (String) -> Unit
        ) {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addressList = geocoder.getFromLocationName(query, 1)
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    coroutineScope.launch {
                        camerapositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                    onSuccess(latLng)
                } else {
                    onError("Location not found")
                }
            } catch (e: Exception) {
                onError("Error searching for location")
                e.printStackTrace()
            }
        }

        LaunchedEffect(currentLocation) {
            currentLocation?.let {
                currentAddress = getAddressFromLatLng(it)
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
            Box(modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus() // Ẩn bàn phím khi tap ra ngoài
                })
            }) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camerapositionState
                ) {
                    if (isLocationUpdated && currentLocation != null) {
                        Marker(
                            state = MarkerState(position = currentLocation!!),
                            title = "Your location",
                            snippet = "Current address",
                            icon = smallIcon,
                        )
                    }

                    nearbyUsersLocations.forEach { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Nearby users",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                LaunchedEffect(isLocationUpdated) {
                    if (isLocationUpdated && currentLocation != null) {
                        camerapositionState.animate(CameraUpdateFactory.newLatLng(currentLocation!!))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Tìm kiếm vị trí

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = Color.Black.copy(alpha = 0.1f)
                            )
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF8F9FA)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        placeholder = {
                            Text(
                                "🔍 Enter location to search...",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            keyboardType = KeyboardType.Text,
                            autoCorrect = true,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                // Xử lý tìm kiếm khi nhấn nút Search trên bàn phím

                                if (searchQuery.isNotBlank()) {
                                    searchLocation(
                                        query = searchQuery,
                                        camerapositionState = camerapositionState,
                                        coroutineScope = coroutineScope,
                                        onSuccess = { latLng ->
                                            currentLocation = latLng
                                            isLocationUpdated = true
                                            searchError = null
                                            // Ẩn bàn phím sau khi search
                                            keyboardController?.hide()
                                        },
                                        onError = { errorMsg ->
                                            searchError = errorMsg
                                        }
                                    )
                                } else {
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF2196F3),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        trailingIcon = {
                            val coroutineScope = rememberCoroutineScope()

                            // Hiệu ứng scale khi click
                            var buttonScale by remember { mutableStateOf(1f) }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF2196F3),
                                                Color(0xFF1976D2)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        buttonScale = 0.9f
                                        if (searchQuery.isNotBlank()) {
                                            searchLocation(
                                                query = searchQuery,
                                                camerapositionState = camerapositionState,
                                                coroutineScope = coroutineScope,
                                                onSuccess = { latLng ->
                                                    currentLocation = latLng
                                                    isLocationUpdated = true
                                                    searchError = null
                                                    // Ẩn bàn phím
                                                    keyboardController?.hide()
                                                },
                                                onError = { errorMsg ->
                                                    searchError = errorMsg
                                                }
                                            )
                                        }
                                        // Reset scale sau khi click
                                        buttonScale = 1f
                                    }
                                    .scale(buttonScale),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_search_24),
                                    contentDescription = "Search location",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )


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
                    Button(
                        onClick = {
                            if (permissions.all {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        it
                                    ) == PackageManager.PERMISSION_GRANTED
                                }) {
                                val locationManager =
                                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                val isGpsEnabled =
                                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                                if (isGpsEnabled) {
                                    startLocationUpdates(fusedLocationClient, context) { location ->
                                        currentLocation = location
                                        isLocationUpdated = true
                                    }
                                } else {
                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    context.startActivity(intent)
                                }
                            } else {
                                launchMultiplePermissions.launch(permissions)
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(50.dp)

                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Update Location", fontWeight = FontWeight.SemiBold)
                            }
                        }
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
            Toast.makeText(context, "Unable to load image from Firestore", Toast.LENGTH_SHORT)
                .show()
        })
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = { navController.navigate("profile") },
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_location_on_24),
                            contentDescription = "Location",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Vị trí hiện tại",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = currentAddress,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                EnhancedProfileButton(
                    navController = navController,
                    userPhotoUrl = userPhotoUrl
                )
            }
        }
    }

    @Composable
    fun EnhancedProfileButton(
        navController: NavController,
        userPhotoUrl: String?
    ) {
        var imageUrl by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        loadImageUrlFromFirestore({ url ->
            imageUrl = url
        }, {
            Toast.makeText(context, "Unable to load image from Firestore", Toast.LENGTH_SHORT)
                .show()
        })

        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            IconButton(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE1BEE7),
                                    Color(0xFFCE93D8)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageUrl != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrl),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.defaultimg),
                                contentDescription = "Default Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                    )
                }
            }
        }
    }
}