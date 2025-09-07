package com.example.projectdemo.feature.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object LoggedIn : AuthState()
    data class Authenticated(
        val userId: String,
        val userName: String,
        val dateOfBirth: String
    ) : AuthState()
    data class Error(val message: String) : AuthState()
    data class AccountCreated(val message: String) : AuthState()
}


class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val firebaseAuth = FirebaseAuth.getInstance()


    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                Firebase.firestore.collection("profile").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val userName = document.getString("userName") ?: "Unknown"
                            val dateOfBirth = document.getString("dateOfBirth") ?: "Unknown"
                            _authState.value =
                                AuthState.Authenticated(userId, userName, dateOfBirth)
                        } else {
                            _authState.value = AuthState.Error("User data not found")
                        }
                    }
                    .addOnFailureListener { e ->
                        _authState.value =
                            AuthState.Error(e.message ?: "Error retrieving user data")
                    }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email hoặc mật khẩu không được để trống")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        if (!firebaseUser.isEmailVerified) {
                            auth.signOut()
                            _authState.value =
                                AuthState.Error("Tài khoản chưa được xác thực. Vui lòng kiểm tra email.")
                            return@addOnCompleteListener
                        }

                        val userId = firebaseUser.uid
                        Firebase.firestore.collection("profile").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userName = document.getString("userName") ?: "Unknown"
                                    val dateOfBirth = document.getString("dateOfBirth") ?: "Unknown"
                                    _authState.value =
                                        AuthState.Authenticated(userId, userName, dateOfBirth)
                                } else {
                                    _authState.value =
                                        AuthState.Error("Không tìm thấy dữ liệu người dùng")
                                }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(
                                    e.message ?: "Lỗi khi lấy dữ liệu người dùng"
                                )
                            }
                    } else {
                        _authState.value = AuthState.Error("User null sau khi đăng nhập")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Đăng nhập thất bại")
                }
            }
    }


    fun signup(account: String, password: String, userName: String, dateOfBirth: String) {
        Firebase.auth.fetchSignInMethodsForEmail(account)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        Firebase.auth.createUserWithEmailAndPassword(account, password)
                            .addOnCompleteListener { createTask ->
                                if (createTask.isSuccessful) {
                                    val userId = createTask.result?.user?.uid
                                    val firebaseUser = createTask.result?.user
                                    if (userId != null && firebaseUser != null) {

                                        // Gửi email xác thực
                                        firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    val user = hashMapOf(
                                                        "userName" to userName,
                                                        "dateOfBirth" to dateOfBirth
                                                    )
                                                    Firebase.firestore.collection("profile")
                                                        .document(userId)
                                                        .set(user)
                                                        .addOnSuccessListener {
                                                            _authState.postValue(
                                                                AuthState.AccountCreated("Tạo tài khoản thành công! Vui lòng kiểm tra email để xác thực.")
                                                            )
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            _authState.postValue(
                                                                AuthState.Error(
                                                                    exception.message ?: "Lỗi khi lưu dữ liệu người dùng"
                                                                )
                                                            )
                                                        }
                                                } else {
                                                    _authState.postValue(
                                                        AuthState.Error("Không gửi được email xác thực: ${verifyTask.exception?.message}")
                                                    )
                                                }
                                            }

                                    } else {
                                        _authState.postValue(AuthState.Error("Không lấy được userId"))
                                    }
                                } else {
                                    _authState.postValue(
                                        AuthState.Error(
                                            createTask.exception?.message ?: "Lỗi khi tạo user"
                                        )
                                    )
                                }
                            }
                    } else {
                        _authState.postValue(AuthState.Error("Tài khoản đã tồn tại"))
                    }
                } else {
                    _authState.postValue(
                        AuthState.Error(task.exception?.message ?: "Lỗi khi kiểm tra tài khoản")
                    )
                }
            }
    }



    fun signout(navController: NavController, context: Context) {
        auth.signOut()
        val googleSignInClient = GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        )
        googleSignInClient.signOut().addOnCompleteListener {
            _authState.value = AuthState.Unauthenticated
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

}