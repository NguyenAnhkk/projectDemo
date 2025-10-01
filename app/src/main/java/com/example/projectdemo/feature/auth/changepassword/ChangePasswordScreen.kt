package com.example.projectdemo.feature.auth.changepassword

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

private val ModernColorScheme = lightColorScheme(
    primary = Color(0xFF667EEA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF001D33),
    background = Color(0xFFFAFBFE),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFFE0E0E0),
    error = Color(0xFFE74C3C)
)

private val GradientColors = listOf(
    Color(0xFF667EEA),
    Color(0xFF764BA2)
)

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
    val scrollState = rememberScrollState()
    var passwordVisibility by remember {
        mutableStateOf(false)
    }
    var icon = if (passwordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }
    fun changePassword() {
        if (isLoading) return

        try {
            errorMessage = null
            isLoading = true

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                isLoading = false
                return
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(context, "New passwords do not match!", Toast.LENGTH_SHORT).show()
                isLoading = false
                return
            }

            if (newPassword.length < 6) {
                Toast.makeText(
                    context,
                    "Password must be at least 6 characters long!",
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
                return
            }

            if (newPassword == currentPassword) {
                Toast.makeText(
                    context,
                    "New password must be different from current password!",
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
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
                Toast.makeText(
                    context,
                    "This account does not support password change (no email linked)!",
                    Toast.LENGTH_LONG
                ).show()
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
                                    Toast.makeText(
                                        context,
                                        "Password changed successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
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

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    AppScreen(
        backgroundColor = ModernColorScheme.background,
        isPaddingNavigation = true,
        modifier = Modifier.fillMaxSize()
    ) {
        AppColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ModernColorScheme.background,
                            Color(0xFFF8FAFF)
                        )
                    )
                )
        ) {
            AppRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = ModernColorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ModernColorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                AppText(
                    text = "Change Password",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ModernColorScheme.onSurface,
                    fontSize = 20.sp
                )
            }

            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
            ) {
                AppBox(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.linearGradient(colors = GradientColors),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_lock_reset_24),
                        contentDescription = "Change Password",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                AppColumn(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        text = "Update Your Password",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ModernColorScheme.onSurface,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AppText(
                        text = "Enter your current password and set a new one to secure your account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ModernColorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Form Section
                AppColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Current Password Field
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = {
                            AppText(
                                text = "Current Password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ModernColorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ModernColorScheme.primary,
                            unfocusedBorderColor = ModernColorScheme.outline,
                            focusedLabelColor = ModernColorScheme.primary,
                            cursorColor = ModernColorScheme.primary,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (currentPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { currentPasswordVisibility = !currentPasswordVisibility },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                    tint = ModernColorScheme.onSurfaceVariant
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = ModernColorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ModernColorScheme.primary,
                            unfocusedBorderColor = ModernColorScheme.outline,
                            focusedLabelColor = ModernColorScheme.primary,
                            cursorColor = ModernColorScheme.primary,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { newPasswordVisibility = !newPasswordVisibility },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                    tint = ModernColorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = {
                            AppText(
                                text = "Confirm New Password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ModernColorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ModernColorScheme.primary,
                            unfocusedBorderColor = ModernColorScheme.outline,
                            focusedLabelColor = ModernColorScheme.primary,
                            cursorColor = ModernColorScheme.primary,
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { confirmPasswordVisibility = !confirmPasswordVisibility },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                    tint = ModernColorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true
                    )

                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ModernColorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, ModernColorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        AppRow(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_info_24),
                                contentDescription = "Info",
                                tint = ModernColorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 12.dp)
                            )
                            AppText(
                                text = "New password must be at least 6 characters long and different from current password",
                                style = MaterialTheme.typography.bodySmall,
                                color = ModernColorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Action Buttons
                AppColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { changePassword() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModernColorScheme.primary,
                            disabledContainerColor = ModernColorScheme.primary.copy(alpha = 0.5f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
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
                            AppText(
                                text = "Update Password",
                                color = Color.White,
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AppText(
                            text = "Cancel",
                            color = ModernColorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}