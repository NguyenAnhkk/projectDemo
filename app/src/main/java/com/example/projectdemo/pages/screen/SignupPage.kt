package com.example.projectdemo.pages.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
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
import com.example.projectdemo.ui.theme.AuthState
import com.example.projectdemo.ui.theme.AuthViewModel
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

    AppScreen(
        backgroundColor = MyAppTheme.appColor.background,
        isPaddingNavigation = true,
        modifier = Modifier.fillMaxSize()
    ) {
        AppColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            AppColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTextBold(
                    text = "Tạo tài khoản mới",
                    fontSize = 24.sp,
                    color = Color(0xFF405DA3),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AppText(
                    text = "Điền thông tin để đăng ký tài khoản",
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
                    value = account,
                    onValueChange = { account = it },
                    label = { AppText(text = "Email", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { AppText(text = "Mật khẩu", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                painter = passwordIcon,
                                contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { AppText(text = "Tên người dùng", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF405DA3),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF405DA3),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { },
                    label = { AppText(text = "Ngày sinh", color = Color.Gray) },
                    readOnly = true,
                    trailingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                            contentDescription = "Chọn ngày",
                            modifier = Modifier.clickable { calendarState.show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        if (dateOfBirth.isEmpty() || userName.isEmpty() || password.isEmpty() || account.isEmpty()) {
                            Toast.makeText(context, "Thông tin chưa đầy đủ.", Toast.LENGTH_SHORT)
                                .show()
                        } else if (!isGmailAccount(account)) {
                            Toast.makeText(
                                context,
                                "Email phải có định dạng @gmail.com.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (!isOver13YearsOld(dateOfBirth)) {
                            Toast.makeText(
                                context,
                                "Bạn phải trên 16 tuổi để đăng ký tài khoản.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            authViewModel.signup(account, password, userName, dateOfBirth)
                            authViewModel.authState.observe(context as LifecycleOwner) { state ->
                                when (state) {
                                    is AuthState.AccountCreated -> {
                                        Toast.makeText(
                                            context,
                                            "Tạo tài khoản thành công!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("login")
                                    }

                                    is AuthState.Error -> {
                                        if (state.message == "Account already exists") {
                                            Toast.makeText(
                                                context,
                                                "Tài khoản đã tồn tại.",
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
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF405DA3)
                    )
                ) {
                    AppTextBold(
                        text = "Đăng ký",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText(
                        text = "Đã có tài khoản? Đăng nhập ngay!",
                        color = Color(0xFF405DA3)
                    )
                }
            }
        }
    }
}