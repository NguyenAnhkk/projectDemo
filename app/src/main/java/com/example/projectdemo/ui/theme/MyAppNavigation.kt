package com.example.projectdemo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.projectdemo.pages.HomePage
import com.example.projectdemo.pages.LoginPage
import com.example.projectdemo.pages.SignupPage
import com.example.projectdemo.user.Profile
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectdemo.pages.ForgotPasswordScreen
import com.example.projectdemo.pages.PasswordResetViewModel

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("forgot") {
            ForgotPasswordScreen(viewModel = PasswordResetViewModel() , navController)
        }
        composable("signup") {
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("profile") {
            Profile(modifier, navController, authViewModel)
        }
    }
    )
}