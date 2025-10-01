package com.example.projectdemo.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.projectdemo.feature.auth.signin.components.ActionButton
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.example.projectdemo.ui.theme.Pink40

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("animation.json"))
    val gradientColors = listOf(
        Color(0xFFFC466B),
        Color(0xFF3F5EFB),
    )
    AppColumn(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(300.dp),
            iterations = Int.MAX_VALUE
        )

        AppTextBold(
            text = "Let's Get Started",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        AppText(
            text = "Build meaningful connections with our app",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        ActionButton(
            text = "Get Started",
            onClicked = {
                navController.navigate("login") {
                    popUpTo("welcome") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            isNavigationArrowVisible = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = Pink40,
                contentColor = Color.White
            ),
            shadowColor = Pink40.copy(alpha = 0.5f)
        )
    }
}