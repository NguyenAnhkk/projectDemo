package com.example.projectdemo.feature.auth.changepassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projectdemo.R
import com.example.projectdemo.lib.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavHostController) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisibility by remember { mutableStateOf(false) }
    var newPasswordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val currentPasswordIcon = if (currentPasswordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }

    val newPasswordIcon = if (newPasswordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }

    val confirmPasswordIcon = if (confirmPasswordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }

    // Password change handler function
    fun changePassword() {
        if (isLoading) return

        try {
            // Reset error message
            errorMessage = null

            // Validation
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "New passwords do not match!", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword == currentPassword) {
                Toast.makeText(context, "New password must be different from current password!", Toast.LENGTH_SHORT).show()
                return
            }

            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
                isLoading = false
                return
            }

            val email = user.email
            if (email.isNullOrEmpty()) {
                Toast.makeText(context, "This account does not support password change (no email linked)!", Toast.LENGTH_LONG).show()
                isLoading = false
                return
            }

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                isLoading = false

                                if (updateTask.isSuccessful) {
                                    Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    val error = updateTask.exception
                                    val errorMsg = when {
                                        error?.message?.contains("requires recent authentication") == true ->
                                            "Please log out and log in again to change password."
                                        error?.message?.contains("password is too weak") == true ->
                                            "Password is too weak. Please choose a stronger password."
                                        else ->
                                            "Password change failed: ${error?.message ?: "Unknown error"}"
                                    }
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        isLoading = false
                        val error = reauthTask.exception
                        val errorMsg = when {
                            error?.message?.contains("wrong-password") == true ->
                                "Current password is incorrect!"
                            error?.message?.contains("invalid-email") == true ->
                                "Invalid email address!"
                            error?.message?.contains("network error") == true ->
                                "Network error. Please check your connection."
                            else ->
                                "Authentication failed: ${error?.message ?: "Unknown error"}"
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Hiển thị error message nếu có
    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    AppScreen(
        backgroundColor = MyAppTheme.appColor.background,
        isPaddingNavigation = true,
        modifier = Modifier.fillMaxSize()
    ) {
        AppColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with title
            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_lock_reset_24),
                    contentDescription = "Change Password",
                    tint = Color(0xFF405DA3),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                AppTextBold(
                    text = "Change Password",
                    fontSize = 28.sp,
                    color = Color(0xFF405DA3),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AppText(
                    text = "Please enter your current password and new password",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = {
                        AppText(
                            text = "Current Password",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FF),
                        unfocusedContainerColor = Color(0xFFF8F9FF),
                        focusedIndicatorColor = Color(0xFF405DA3),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color(0xFF405DA3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (currentPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { currentPasswordVisibility = !currentPasswordVisibility },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = currentPasswordIcon,
                                contentDescription = if (currentPasswordVisibility) "Hide password" else "Show password",
                                tint = Color(0xFF666666)
                            )
                        }
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = {
                        AppText(
                            text = "New Password",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FF),
                        unfocusedContainerColor = Color(0xFFF8F9FF),
                        focusedIndicatorColor = Color(0xFF405DA3),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color(0xFF405DA3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { newPasswordVisibility = !newPasswordVisibility },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = newPasswordIcon,
                                contentDescription = if (newPasswordVisibility) "Hide password" else "Show password",
                                tint = Color(0xFF666666)
                            )
                        }
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = {
                        AppText(
                            text = "Confirm New Password",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FF),
                        unfocusedContainerColor = Color(0xFFF8F9FF),
                        focusedIndicatorColor = Color(0xFF405DA3),
                        unfocusedIndicatorColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color(0xFF405DA3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { confirmPasswordVisibility = !confirmPasswordVisibility },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = confirmPasswordIcon,
                                contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password",
                                tint = Color(0xFF666666)
                            )
                        }
                    },
                    singleLine = true
                )

                Button(
                    onClick = { changePassword() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF405DA3),
                        disabledContainerColor = Color(0xFFA0A8C6)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        AppTextBold(
                            text = "Change Password",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = {
                        if (!isLoading) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    AppText(
                        text = "Back",
                        color = Color(0xFF405DA3),
                        fontSize = 16.sp
                    )
                }
            }

            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F4FF)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    AppText(
                        text = "New password must be at least 6 characters long",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                    )
                }
            }
        }
    }
}