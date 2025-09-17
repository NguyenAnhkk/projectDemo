package com.example.projectdemo.feature.sign_in

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec

import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("relationship.json"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(composition = composition, modifier = Modifier.size(300.dp))
        Text(text ="Let start app")
    }
}
