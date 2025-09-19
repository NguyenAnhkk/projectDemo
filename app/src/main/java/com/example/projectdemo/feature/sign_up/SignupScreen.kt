package com.example.projectdemo.feature.sign_up

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.example.projectdemo.lib.MyAppTheme
import com.example.projectdemo.feature.viewmodel.AuthState
import com.example.projectdemo.feature.viewmodel.AuthViewModel
import com.example.projectdemo.ui.theme.rememberImeState
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisibility by remember { mutableStateOf(false) }
    val calendarState = rememberSheetState()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!]).{8,}\$")
        return passwordPattern.matches(password)
    }

    val passwordIcon = if (passwordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(monthSelection = false, yearSelection = true),
        selection = CalendarSelection.Date { date ->
            dateOfBirth = "${date.dayOfMonth}/${date.monthValue}/${date.year}"
            showError = false
        }
    )

    fun isOver13YearsOld(dateOfBirth: String): Boolean {
        if (dateOfBirth.isEmpty()) return false
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        val birthDate = LocalDate.parse(dateOfBirth, formatter)
        val currentDate = LocalDate.now()
        val age = ChronoUnit.YEARS.between(birthDate, currentDate)
        return age >= 13
    }

    fun isGmailAccount(account: String): Boolean {
        return account.endsWith("@gmail.com")
    }

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    // Background gradient colors - matching the login screen
    val gradientColors = listOf(
        Color(0xFFFC466B),  // Vibrant pink
        Color(0xFF3F5EFB),  // Bright blue
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
    ) {

        AppScreen(
            backgroundColor = Color.Transparent,
            isPaddingNavigation = true,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo/Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    AppTextBold(
                        text = "Create Account",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    AppText(
                        text = "Join our community today",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                // Registration Form Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    AppColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = account,
                            onValueChange = { account = it },
                            label = { AppText(text = "Email", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFC466B),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { AppText(text = "Password", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFC466B),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        painter = passwordIcon,
                                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                        tint = Color(0xFFFC466B)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { AppText(text = "Username", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFC466B),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = dateOfBirth,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFC466B),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            onValueChange = { },
                            label = { AppText(text = "Date of Birth", color = Color.Gray) },
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                                    contentDescription = "Select date",
                                    modifier = Modifier
                                        .clickable { calendarState.show() }
                                        .size(24.dp),
                                    tint = Color(0xFFFC466B)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (dateOfBirth.isEmpty() || userName.isEmpty() || password.isEmpty() || account.isEmpty()) {
                                    Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT)
                                        .show()
                                } else if (!isGmailAccount(account)) {
                                    Toast.makeText(
                                        context,
                                        "Email must be a valid Gmail account.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (!isOver13YearsOld(dateOfBirth)) {
                                    Toast.makeText(
                                        context,
                                        "You must be at least 13 years old to register.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (!isValidPassword(password)) {
                                    Toast.makeText(
                                        context,
                                        "Password must be at least 8 characters, including uppercase, lowercase, numbers and special characters.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    authViewModel.signup(account, password, userName, dateOfBirth)
                                    authViewModel.authState.observe(context as LifecycleOwner) { state ->
                                        when (state) {
                                            is AuthState.AccountCreated -> {
                                                Toast.makeText(
                                                    context,
                                                    "Account created successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("login")
                                            }

                                            is AuthState.Error -> {
                                                if (state.message == "Account already exists") {
                                                    Toast.makeText(
                                                        context,
                                                        "Account already exists.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        state.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                            else -> Unit
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFC466B)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            AppTextBold(
                                text = "Sign Up",
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { navController.navigate("login") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppText(
                                text = "Already have an account? Sign in now!",
                                color = Color(0xFFFC466B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Additional decorative element
                AppText(
                    text = "Start your journey with us",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}