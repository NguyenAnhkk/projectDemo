package com.example.projectdemo.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import com.example.projectdemo.pages.screen.HomePage
import com.example.projectdemo.pages.screen.LoginPage
import com.example.projectdemo.pages.screen.SignupPage
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectdemo.pages.forgotpassword.ForgotPasswordScreen
import com.example.projectdemo.pages.forgotpassword.PasswordResetViewModel
import com.example.projectdemo.pages.screen.ChangePasswordScreen
import com.example.projectdemo.pages.screen.Profile
import com.example.projectdemo.pages.screen.UserDetailScreen
import com.example.projectdemo.pages.screen.MatchesScreen
import com.example.projectdemo.pages.screen.VideoCallScreen
import com.example.projectdemo.viewdata.CourseDetailsActivity
import com.example.projectdemo.viewdata.UpdateDataScreen
import com.google.android.gms.maps.model.LatLng

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            LoginPage(navController, authViewModel)
        }
        composable("forgot") {
            ForgotPasswordScreen(viewModel = PasswordResetViewModel(), navController)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("change_password") {
            ChangePasswordScreen(navController)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("profile") {
            Profile(modifier, navController, authViewModel)
        }
        composable("matches") {
            MatchesScreen(navController)
        }
        composable("course/{lat}/{lng}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            val currentLocation = LatLng(lat, lng)

            CourseDetailsActivity(navController = navController, currentLocation = currentLocation)
        }
        composable("video_call/{channelName}/{userId}/{receiverId}") { backStackEntry ->
            val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            VideoCallScreen(
                navController = navController,
                channelName = channelName,
                userId = userId,
                receiverId = receiverId
            )
        }
        composable("update_data/{name}/{age}/{address}", arguments = listOf(
            navArgument("name") { type = NavType.StringType },
            navArgument("age") { type = NavType.StringType },
            navArgument("address") { type = NavType.StringType }
        )) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val age = backStackEntry.arguments?.getString("age") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""
            UpdateDataScreen(name, age, address)
        }
        composable("user_detail/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(navController = navController, userId = userId)
        }
    })
}
