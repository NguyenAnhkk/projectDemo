
package com.example.projectdemo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.projectdemo.pages.screen.Profile
import com.example.projectdemo.pages.screen.Users
import com.example.projectdemo.sign_in.SignInViewModel
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
