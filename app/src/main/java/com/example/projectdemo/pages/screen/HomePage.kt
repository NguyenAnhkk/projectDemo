package com.example.projectdemo.pages.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectdemo.R
import com.example.projectdemo.pages.map.MapUtils
import com.example.projectdemo.ui.theme.AuthViewModel
import com.example.projectdemo.ui.theme.rememberImeState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
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
        )
    }
    Box() {
        IconButtonWithImage(navController = navController)
    }
}

@Composable
fun IconButtonWithImage(navController: NavController) {
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF9575CD),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
                Color(0xFFFFB74D),
                Color(0xFFFFF176),
                Color(0xFFAED581),
                Color(0xFF4DD0E1),
                Color(0xFF9575CD)
            )
        )
    }
    val borderWidth = 2.dp
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        IconButton(
            onClick = { navController.navigate("profile") },
            modifier = Modifier
                .padding(16.dp),
            content = {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(borderWidth)
                        .clip(CircleShape)
                        .border(BorderStroke(borderWidth, rainbowColorsBrush), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.imgprofile),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
        )
    }
}
