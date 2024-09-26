package com.example.projectdemo.pages.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.projectdemo.R
import com.example.projectdemo.ui.theme.AuthState
import com.example.projectdemo.ui.theme.AuthViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val userName = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    var imageUrl by remember { mutableStateOf<String?>(null) }
    val currentLocation = LatLng(21.0278, 105.8342)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Truy cập bộ nhớ thất bại.", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                uploadImageToStorage(uri, { url ->
                    imageUrl = url
                    saveImageUrlToFirestore(url)
                }, {
                    Toast.makeText(context, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    fun pickImage() {
        if (hasPermission) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        } else {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val authenticatedState = authState as AuthState.Authenticated
            userName.value = authenticatedState.userName
            val dateOfBirthString = authenticatedState.dateOfBirth
            val dateOfBirth = convertDateOfBirth(dateOfBirthString)
            val calculatedAge = dateOfBirth?.let { calculateAge(it) } ?: "Không xác định"
            age.value = calculatedAge.toString()

            loadImageUrlFromFirestore({ url ->
                imageUrl = url
            }, {
                Toast.makeText(context, "Không thể tải ảnh từ Firestore", Toast.LENGTH_SHORT).show()
            })
        } else if (authState is AuthState.Error) {
            Toast.makeText(
                context,
                "Lỗi: ${(authState as AuthState.Error).message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier.clip(RoundedCornerShape(topEnd = 30.dp, topStart = 30.dp)),
                containerColor = Color(0xFFdab5f5)
            ) {
                Row(
                    modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                    }
                    FloatingActionButton(
                        onClick = { navController.navigate("course/${currentLocation.latitude}/${currentLocation.longitude}") },
                        containerColor = Color(0xFFeffcc2)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "add",
                        )
                    }
                    IconButton(onClick = { authViewModel.signout(navController, context) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = null
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFFFFF))
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable { pickImage() }
                    ) {
                        when {
                            imageUri != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUri),
                                    contentDescription = "Selected Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            imageUrl != null -> {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = "User Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            else -> {
                                Image(
                                    painter = painterResource(id = R.drawable.defaultimg),
                                    contentDescription = "Default Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            Text(
                                text = userName.value,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Age: ${age.value}")
                        }

                        is AuthState.Error -> {
                            Text(text = (authState as AuthState.Error).message, color = Color.Red)
                        }

                        else -> {
                            Text(text = "Loading...")
                        }
                    }
                }
            }
        }
    )
}

fun calculateAge(dateOfBirth: LocalDate): Int {
    val today = LocalDate.now()
    return Period.between(dateOfBirth, today).years
}

fun convertDateOfBirth(dateOfBirthString: String): LocalDate? {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    return try {
        LocalDate.parse(dateOfBirthString, formatter)
    } catch (e: Exception) {
        null
    }
}

fun uploadImageToStorage(
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storageReference = FirebaseStorage.getInstance().reference
    val fileName = UUID.randomUUID().toString()
    val fileReference = storageReference.child("images/$fileName")

    fileReference.putFile(imageUri)
        .addOnSuccessListener {
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }
        .addOnFailureListener { exception ->
            Log.e("FirebaseUpload", "Tải ảnh lên thất bại: ${exception.message}", exception)
            onFailure(exception)
        }
}

fun saveImageUrlToFirestore(imageUrl: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        val imageInfo = hashMapOf("imageUrl" to imageUrl, "timestamp" to System.currentTimeMillis())

        firestore.collection("users")
            .document(userId)
            .collection("images")
            .add(imageInfo)
            .addOnSuccessListener {
                Log.d("Firestore", "Ảnh đã được lưu thành công.")
            }
            .addOnFailureListener { exception ->
                Log.d("Error", "${exception.message}", exception)
            }
    }
}

fun loadImageUrlFromFirestore(onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        firestore.collection("users")
            .document(userId)
            .collection("images")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val imageUrl = result.documents[0].getString("imageUrl")
                    onSuccess(imageUrl)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}