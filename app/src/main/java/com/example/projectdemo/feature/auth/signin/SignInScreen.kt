package com.example.projectdemo.feature.auth.signin

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.example.projectdemo.ui.theme.rememberImeState
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import com.example.projectdemo.feature.auth.common.AuthState
import com.example.projectdemo.feature.auth.common.AuthViewModel
import com.example.projectdemo.lib.AppBox
import com.example.projectdemo.lib.AppRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val callbackManager = remember { CallbackManager.Factory.create() }
    fun handleFacebookAccessToken(token: AccessToken, navController: NavHostController) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FacebookLogin", "signInWithCredential:success")
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home")
                } else {
                    Log.w("FacebookLogin", "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    val facebookCallback = remember {
        object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken, navController)
            }

            override fun onCancel() {
                Log.d("FacebookLogin", "Login canceled")
            }

            override fun onError(error: FacebookException) {
                Log.d("FacebookLogin", "Login error: ${error.message}")
            }
        }
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState = authViewModel.authState.observeAsState()
    val imeState = rememberImeState()
    val scrollState = rememberScrollState()
    var passwordVisibility by remember {
        mutableStateOf(false)
    }
    var icon = if (passwordVisibility) {
        painterResource(id = R.drawable.baseline_remove_red_eye_24)
    } else {
        painterResource(id = R.drawable.baseline_visibility_off_24)
    }
    var user by remember {
        mutableStateOf(Firebase.auth.currentUser)
    }
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = { result -> user = result.user },
        onAuthError = { user = null }
    )
    val token = stringResource(id = R.string.client_id)
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    LaunchedEffect(Unit) {
        if (authViewModel.authState.value is AuthState.Error) {
            authViewModel.checkAuthStatus()
        }
    }
    LaunchedEffect(callbackManager) {
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback)
    }

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            val targetScroll = scrollState.maxValue / 3
            scrollState.animateScrollTo(targetScroll, tween(300))
        } else {
            scrollState.animateScrollTo(0, tween(300))
        }
    }
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                if (errorMessage != "Email or password cannot be empty") {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            else -> Unit
        }
    }

    val gradientColors = listOf(
        Color(0xFFFC466B),
        Color(0xFF3F5EFB),
    )

    AppBox(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        AppScreen(
            backgroundColor = Color.Transparent,
            isPaddingNavigation = true,
            modifier = Modifier.fillMaxSize()
        ) {
            AppColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppTextBold(
                        text = "Welcome Back",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                    AppText(
                        text = "Sign in to continue your journey",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                AppBox(
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
                            value = email,
                            onValueChange = { email = it },
                            label = { AppText(text = "Email", color = Color(0xFF1f1f1f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedBorderColor = Color(0xFFFC466B),
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { AppText(text = "Password", color = Color(0xFF1f1f1f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedBorderColor = Color(0xFFFC466B),
                                cursorColor = Color(0xFFFC466B),
                                containerColor = Color.White
                            ),
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(
                                        painter = icon,
                                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                                        tint = Color(0xFFFC466B)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        TextButton(
                            onClick = { navController.navigate("forgot") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            AppText(
                                text = "Forgot Password?",
                                color = Color(0xFFFC466B)
                            )
                        }

                        Button(
                            onClick = {
                                authViewModel.login(email, password, isUserInitiated = true)
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
                                text = "Sign In",
                                color = Color.White
                            )
                        }

                        DividerWithText()

                        Button(
                            onClick = { navController.navigate("signup") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(2.dp, Color(0xFFFC466B))
                        ) {
                            AppTextBold(
                                text = "Create Account",
                                color = Color(0xFFFC466B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                AppText(
                    text = "Connect with amazing people",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DividerWithText() {
    AppRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier
                .weight(1f)
        )
        AppText(
            text = "or",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )
        Divider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleAuth", "account $account")
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            Log.d("GoogleAuth", e.toString())
            onAuthError(e)
        }
    }
}