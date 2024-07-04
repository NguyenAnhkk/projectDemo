package com.example.projectdemo.pages

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projectdemo.R
import com.example.projectdemo.ui.theme.AuthState
import com.example.projectdemo.ui.theme.AuthViewModel
import com.example.projectdemo.ui.theme.rememberImeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.AccountCreated -> navController.navigate("login")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
            Box(contentAlignment = Alignment.TopCenter) {
                Image(
                    painter = painterResource(id = R.drawable.imglogin),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier.fillMaxHeight(0.3f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Signup Page", fontSize = 32.sp)
                }

            }
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFffffff))) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = {
                        Text(text = "Email")
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = password, onValueChange = {
                        password = it
                    }, label = { Text(text = "Password") })
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.signup(email, password) },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text(text = "Create account")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text(text = "Alrealdy have an account , login")
                    }
                }
            }
        }
    }

}