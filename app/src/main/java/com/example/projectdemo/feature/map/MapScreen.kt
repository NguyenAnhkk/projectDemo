package com.example.projectdemo.feature.map

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.projectdemo.feature.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val userPhotoUrl = user?.photoUrl?.toString()
    var currentLocation by remember {
        mutableStateOf(LatLng(21.0278, 105.8342))
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation, 15f
        )
    }
    val mapUtils = MapUtils(context)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        mapUtils.LocationScreen(
            context = context,
            currentLocation = currentLocation,
            camerapositionState = cameraPositionState,
            navController = navController
        )
    }
}
