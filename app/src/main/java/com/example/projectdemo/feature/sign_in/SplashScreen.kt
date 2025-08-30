package com.example.projectdemo.feature.sign_in

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.projectdemo.R

@Composable
fun SplashScreen(navController: NavController) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splashanimation)
    )


    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )
    val alpha by animateFloatAsState(
        targetValue = if (progress < 1f) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )
    LaunchedEffect(progress) {
        if (progress == 1f) {
            navController.navigate("login"){
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer(alpha = alpha)
        )
    }
}
