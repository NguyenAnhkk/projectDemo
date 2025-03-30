package com.example.projectdemo.pages.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.example.projectdemo.lib.MyAppTheme
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
    val context = LocalContext.current

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
                AppTextBold(
                    text = "Đổi mật khẩu",
                    fontSize = 24.sp,
                    color = Color(0xFF405DA3),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AppText(
                    text = "Vui lòng nhập mật khẩu hiện tại và mật khẩu mới",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // Middle section with form
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
                    label = { AppText(text = "Mật khẩu hiện tại", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    ),
                    visualTransformation = if (currentPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentPasswordVisibility = !currentPasswordVisibility }) {
                            Icon(
                                painter = currentPasswordIcon,
                                contentDescription = if (currentPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { AppText(text = "Mật khẩu mới", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    ),
                    visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisibility = !newPasswordVisibility }) {
                            Icon(
                                painter = newPasswordIcon,
                                contentDescription = if (newPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { AppText(text = "Xác nhận mật khẩu mới", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    ),
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                            Icon(
                                painter = confirmPasswordIcon,
                                contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("profile") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Đổi mật khẩu thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF405DA3)
                    )
                ) {
                    AppTextBold(
                        text = "Đổi mật khẩu",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText(
                        text = "Quay lại",
                        color = Color(0xFF405DA3)
                    )
                }
            }

            // Bottom section with info
            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppText(
                    text = "Mật khẩu mới phải có ít nhất 6 ký tự",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
} 