package com.example.projectdemo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import com.example.projectdemo.pages.HomePage
import com.example.projectdemo.pages.LoginPage
import com.example.projectdemo.pages.SignupPage
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectdemo.pages.ForgotPasswordScreen
import com.example.projectdemo.pages.PasswordResetViewModel
import com.example.projectdemo.user.Profile
import com.example.projectdemo.user.Users
import com.example.projectdemo.viewdata.CourseDetailsActivity
import com.example.projectdemo.viewdata.UpdateDataScreen

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
        composable("users") {
            Users(modifier, navController, authViewModel)
        }
        composable("course") {
            CourseDetailsActivity(navController)
        }
        composable("update_data/{name}/{age}/{address}", arguments = listOf(
            navArgument("name") { type = NavType.StringType },
            navArgument("age") { type = NavType.StringType },
            navArgument("address") { type = NavType.StringType }
        )){
                backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val age = backStackEntry.arguments?.getString("age") ?: ""
            val address = backStackEntry.arguments?.getString("address") ?: ""
            UpdateDataScreen(name, age, address)
        }
    }
    )
}