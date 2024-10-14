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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.example.projectdemo.R
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

    var account by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var userName by remember {
        mutableStateOf("")
    }
    var dateOfBirth by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val calendarState = rememberSheetState()
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(monthSelection = false, yearSelection = true),
        selection = CalendarSelection.Date { date ->
            dateOfBirth = "${date.dayOfMonth}/${date.monthValue}/${date.year}"
            showError = false
        }
    )
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
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
//    LaunchedEffect(authState.value) {
//        when (authState.value) {
//            is AuthState.AccountCreated -> navController.navigate("login") {
//                popUpTo("signup") { inclusive = true }
//            }
//            is AuthState.LoggedIn -> navController.navigate("home")
//            is AuthState.Error -> Toast.makeText(
//                context,
//                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
//            ).show()
//
//            else -> Unit
//        }
//    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFffffff))
            ) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(value = account, onValueChange = { account = it }, label = {
                        Text(text = "Tên đăng nhập")
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = password, onValueChange = {
                        password = it
                    }, label = { Text(text = "Mật khẩu") })
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = userName, onValueChange = { userName = it }, label = {
                        Text(text = "Tên người dùng")
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.calendar),
                        contentDescription = "Select Date",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { calendarState.show() }
                    )
                    Text(text = "Ngày sinh của bạn : $dateOfBirth")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (dateOfBirth.isEmpty() || userName.isEmpty() || password.isEmpty() || account.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Thông tin chưa đầy đủ.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (!isGmailAccount(account)) {
                                Toast.makeText(
                                    context,
                                    "Email phải có định dạng @gmail.com.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (!isOver13YearsOld(dateOfBirth)) {
                                Toast.makeText(
                                    context,
                                    "Bạn phải trên 13 tuổi để đăng ký tài khoản.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                authViewModel.signup(
                                    account,
                                    password,
                                    userName,
                                    dateOfBirth
                                )
                                authViewModel.authState.observe(context as LifecycleOwner) { state ->
                                    when (state) {
                                        is AuthState.AccountCreated -> {
                                            Toast.makeText(context, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login")
                                        }
                                        is AuthState.Error -> {
                                            if (state.message == "Account already exists") {
                                                Toast.makeText(context, "Tài khoản đã tồn tại.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        else -> Unit
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text(text = "Create account")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text(text = "Tôi đã có tài khoản. Đăng nhập ngay!")
                    }
                }
            }
        }
    }
}