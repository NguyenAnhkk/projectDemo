package com.example.projectdemo.feature.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.MyAppTheme
import com.example.projectdemo.feature.viewmodel.AuthState
import com.example.projectdemo.feature.viewmodel.AuthViewModel
import com.example.projectdemo.feature.course.createMatch
import com.example.projectdemo.feature.course.handleIgnore
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.launch

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
    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showNotifications by remember { mutableStateOf(false) }
    var showLikedUsers by remember { mutableStateOf(false) }
    var likedUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Listen for notifications
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    notifications = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply {
                            put("id", doc.id)
                        }
                    }
                }
        }
    }

    // Listen for liked users
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("likes")
                .whereEqualTo("fromUserId", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val likedUsersList = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply {
                            put("id", doc.id)
                            put("lastMessageTime", 0L) // Initialize with default value
                        }
                    }.toMutableList()

                    // Get the most recent message for each liked user
                    val tasks = likedUsersList.map { like ->
                        val toUserId = like["toUserId"] as? String
                        if (toUserId != null) {
                            firestore.collection("chats")
                                .document("$currentUserId-$toUserId")
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { messageSnapshot ->
                                    if (!messageSnapshot.isEmpty) {
                                        val lastMessage = messageSnapshot.documents[0]
                                        like["lastMessageTime"] =
                                            lastMessage.getLong("timestamp") ?: 0L
                                    }
                                }
                        }
                    }

                    // Update the list after all tasks complete
                    launch {
                        // Wait for all tasks to complete
                        tasks.forEach { task ->
                            try {
                                Log.e("Profile", "Error waiting for task")
                            } catch (e: Exception) {
                                Log.e("Profile", "Error waiting for task", e)
                            }
                        }

                        // Sort the list by last message time
                        val sortedList = likedUsersList.sortedByDescending {
                            (it["lastMessageTime"] as? Long) ?: 0L
                        }
                        likedUsers = sortedList
                    }
                }
        }
    }

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
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { navController.navigate("matches") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_chat_24),
                            contentDescription = "Matches",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = { showNotifications = !showNotifications }) {
                        Box {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_24),
                                contentDescription = "Notifications",
                                tint = Color.Black
                            )
                            val unreadCount =
                                notifications.count { !(it["read"] as? Boolean ?: false) }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                    FloatingActionButton(
                        onClick = { navController.navigate("course/${currentLocation.latitude}/${currentLocation.longitude}") },
                        containerColor = Color(0xFFeffcc2)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_people_outline_24),
                            contentDescription = "add",
                        )
                    }
                    IconButton(onClick = { authViewModel.signout(navController, context) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = null, tint = Color.Black
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            AppScreen(
                backgroundColor = MyAppTheme.appColor.background,
                isPaddingNavigation = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    showNotifications -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(notifications) { notification ->
                                val type = notification["type"] as? String
                                val fromUserId = notification["fromUserId"] as? String
                                val timestamp = notification["timestamp"] as? Long
                                val read = notification["read"] as? Boolean ?: false

                                if (type == "like" && fromUserId != null) {
                                    var likedUserName by remember { mutableStateOf("") }

                                    LaunchedEffect(fromUserId) {
                                        FirebaseFirestore.getInstance()
                                            .collection("profile")
                                            .document(fromUserId)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                likedUserName =
                                                    doc.getString("userName") ?: "Unknown User"
                                            }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .clickable {
                                                // Mark notification as read
                                                if (!read && currentUserId != null) {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("users")
                                                        .document(currentUserId)
                                                        .collection("notifications")
                                                        .document(notification["id"] as String)
                                                        .update("read", true)
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (!read) Color(0xFFE3F2FD) else Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = null,
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$likedUserName liked your profile",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }

                                            if (!read) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            // Handle agree
                                                            if (currentUserId != null) {
                                                                handleLike(
                                                                    currentUserId,
                                                                    fromUserId
                                                                )
                                                                // Mark notification as read
                                                                FirebaseFirestore.getInstance()
                                                                    .collection("users")
                                                                    .document(currentUserId)
                                                                    .collection("notifications")
                                                                    .document(notification["id"] as String)
                                                                    .update("read", true)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFF4CAF50)
                                                        )
                                                    ) {
                                                        Text("Agree")
                                                    }

                                                    Button(
                                                        onClick = {
                                                            // Handle ignore
                                                            if (currentUserId != null) {
                                                                handleIgnore(
                                                                    currentUserId,
                                                                    fromUserId
                                                                )
                                                                // Mark notification as read
                                                                FirebaseFirestore.getInstance()
                                                                    .collection("users")
                                                                    .document(currentUserId)
                                                                    .collection("notifications")
                                                                    .document(notification["id"] as String)
                                                                    .update("read", true)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFF44336)
                                                        )
                                                    ) {
                                                        Text("Ignore")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    showLikedUsers -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(likedUsers) { like ->
                                var likedUserName by remember { mutableStateOf("") }
                                var likedUserImage by remember { mutableStateOf<String?>(null) }

                                LaunchedEffect(like["toUserId"]) {
                                    val toUserId = like["toUserId"] as? String
                                    if (toUserId != null) {
                                        // Get user profile
                                        FirebaseFirestore.getInstance()
                                            .collection("profile")
                                            .document(toUserId)
                                            .get()
                                            .addOnSuccessListener { doc ->
                                                likedUserName =
                                                    doc.getString("userName") ?: "Unknown User"
                                            }

                                        // Get user image
                                        FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(toUserId)
                                            .collection("images")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .limit(1)
                                            .get()
                                            .addOnSuccessListener { result ->
                                                if (!result.isEmpty) {
                                                    likedUserImage =
                                                        result.documents[0].getString("imageUrl")
                                                }
                                            }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable {
                                            val toUserId = like["toUserId"] as? String
                                            if (toUserId != null) {
                                                try {
                                                    val bundle = Bundle().apply {
                                                        putString("userId", toUserId)
                                                    }
                                                    navController.navigate("user_detail/$toUserId")
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "Navigation",
                                                        "Error navigating to chat: ${e.message}"
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Error opening chat",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Invalid user",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                        ) {
                                            if (likedUserImage != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(model = likedUserImage),
                                                    contentDescription = "User Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = R.drawable.defaultimg),
                                                    contentDescription = "Default Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = likedUserName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
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

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxSize()

                        ) {
                            when (authState) {
                                is AuthState.Authenticated -> {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().background(Color.Red),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = userName.value,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color.Black
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val shareText = "Xin chào, tôi là ${userName.value}!"

                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                                        type = "text/plain"
                                                    }

                                                    val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ qua")
                                                    context.startActivity(shareIntent)
                                                },
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_share_24),
                                                contentDescription = "share",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Share information")
                                        }
                                    }


                                }

                                is AuthState.Error -> {
                                    Text(
                                        text = (authState as AuthState.Error).message,
                                        color = Color.Red
                                    )
                                }

                                else -> {
                                    Text(
                                        text = "Đang tải dữ liệu người dùng...",
                                        color = Color.Black
                                    )
                                }
                            }
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

fun handleLike(currentUserId: String, likedUserId: String) {
    val firestore = FirebaseFirestore.getInstance()

    // Check if the other user has already liked this user
    firestore.collection("likes")
        .document("$likedUserId-$currentUserId")
        .get()
        .addOnSuccessListener { otherUserLikeDoc ->
            if (otherUserLikeDoc.exists()) {
                // Both users have liked each other - create a match
                createMatch(currentUserId, likedUserId)
                // Remove the like documents since they're now matched
                firestore.collection("likes")
                    .document("$likedUserId-$currentUserId")
                    .delete()
                firestore.collection("likes")
                    .document("$currentUserId-$likedUserId")
                    .delete()
            } else {
                // Just record the like and create a notification
                val likeData = hashMapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "fromUserId" to currentUserId,
                    "toUserId" to likedUserId
                )

                firestore.collection("likes")
                    .document("$currentUserId-$likedUserId")
                    .set(likeData)
                    .addOnSuccessListener {
                        createLikeNotification(currentUserId, likedUserId)
                        Log.d("Like", "Like recorded successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Like", "Error recording like", e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Like", "Error checking for mutual like", e)
        }
}

private fun createLikeNotification(fromUserId: String, toUserId: String) {
    val firestore = FirebaseFirestore.getInstance()

    val notificationData = hashMapOf(
        "type" to "like",
        "fromUserId" to fromUserId,
        "timestamp" to System.currentTimeMillis(),
        "read" to false
    )

    firestore.collection("users")
        .document(toUserId)
        .collection("notifications")
        .add(notificationData)
        .addOnSuccessListener {
            Log.d("LikeNotification", "Like notification created successfully")
        }
        .addOnFailureListener { e ->
            Log.e("LikeNotification", "Error creating like notification", e)
        }
}
