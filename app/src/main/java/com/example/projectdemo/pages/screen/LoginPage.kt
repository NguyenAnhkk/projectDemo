package com.example.projectdemo.pages.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projectdemo.ui.theme.AuthState
import com.example.projectdemo.ui.theme.AuthViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppBox
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppRow
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.example.projectdemo.lib.MyAppTheme
import com.example.projectdemo.sign_in.SignInState
import com.example.projectdemo.ui.theme.rememberImeState
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
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
                    // Xử lý khi đăng nhập thất bại
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

    LaunchedEffect(callbackManager) {
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback)
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
    val launcher = rememberFirebaseAuthLauncher(onAuthComplete = { result -> user = result.user },
        onAuthError = { user = null }
    )
    val token = stringResource(id = R.string.client_id)
    val gso =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(token)
            .requestEmail()
            .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
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
        ) {

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//            ) {
//                AppColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    AppTextBold(
//                        text = "Chào mừng bạn đến với Bave!",
//                        fontSize = 28.sp,
//                        modifier = Modifier.padding(bottom = 8.dp),
//                    )
//                    AppText(
//                        text = "Đăng nhập để tiếp tục",
//                        fontSize = 16.sp,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(bottom = 32.dp)
//                    )
//                }
//            }


                AppBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                AppColumn(
                            modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                        label = { AppText(text = "Tài khoản" , color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Gray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),

                    )

                    // Password Field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                        label = { AppText(text = "Mật khẩu" , color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Gray
                        ),
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    painter = icon,
                                    contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    TextButton(
                        onClick = { navController.navigate("forgot") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        AppText(
                            text = "Quên mật khẩu?",
                            color = Color(0xFF405DA3)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authViewModel.login(email, password)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFd14597)
                        )
                    ) {
                        AppTextBold(
                            text = "Đăng nhập",
                            color = Color.White
                        )
                    }

                    DividerWithText()
                            Button(
                        onClick = { navController.navigate("signup")  },
                        shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF405DA3)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFd14597))
                    ) {
                        AppTextBold(
                            text = "Đăng ký",
                        )
                        }
                    }
                }
            }
    }

}

@Composable
fun DividerWithText() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
        AppText(
            text = "Hoặc",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )
        Divider(
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 1.dp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
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

