package com.example.projectdemo.feature.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.feature.auth.common.AuthState
import com.example.projectdemo.feature.auth.common.AuthViewModel
import com.example.projectdemo.feature.course.createMatch
import com.example.projectdemo.feature.course.handleIgnore
import com.example.projectdemo.feature.map.ManagerLocation
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val unreadCount = notifications.count { !(it["read"] as? Boolean ?: false) }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Profile", "Error listening to notifications", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        notifications = snapshot.documents.mapNotNull { doc ->
                            doc.data?.toMutableMap()?.apply {
                                put("id", doc.id)
                            }
                        }
                    }
                }
        }
    }
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
                            put("lastMessageTime", 0L)
                        }
                    }.toMutableList()

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

                    launch {
                        try {
                            tasks.forEach { task ->
                                try {
                                    Log.e("Profile", "Error waiting for task")
                                } catch (e: Exception) {
                                    Log.e("Profile", "Error waiting for task", e)
                                }
                            }
                            kotlinx.coroutines.delay(500)
                            val sortedList = likedUsersList.sortedByDescending {
                                (it["lastMessageTime"] as? Long) ?: 0L
                            }
                            likedUsers = sortedList
                        } catch (e: Exception) {
                            Log.e("Profile", "Error processing liked users", e)
                        }

                    }
                }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Storage access failed.", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    imageUri = uri
                    uploadImageToStorage(uri, { url ->
                        imageUrl = url
                        saveImageUrlToFirestore(url)
                        Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(
                            context,
                            "Image upload failed: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                } catch (e: Exception) {
                    Toast.makeText(context, "Error accessing image: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun pickImage() {
        if (hasPermission) {
            try {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                    putExtra(
                        Intent.EXTRA_MIME_TYPES,
                        arrayOf("image/jpeg", "image/png", "image/jpg")
                    )
                }
                imagePickerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open photo gallery", Toast.LENGTH_SHORT).show()
            }
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
            val calculatedAge = dateOfBirth?.let { calculateAge(it) } ?: "Unknown"
            age.value = calculatedAge.toString()

            loadImageUrlFromFirestore({ url ->
                imageUrl = url
            }, {
                Toast.makeText(context, "Unable to load image from Firestore", Toast.LENGTH_SHORT).show()
            })
        } else if (authState is AuthState.Error) {
            Toast.makeText(
                context,
                "Error: ${(authState as AuthState.Error).message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("matches") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_chat_24),
                            contentDescription = "Matches",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showNotifications = !showNotifications },
                        modifier = Modifier.size(48.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            if (unreadCount > 9) "9+" else unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_notifications_24),
                                contentDescription = "Notifications",
                                tint = if (showNotifications) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("course/${currentLocation.latitude}/${currentLocation.longitude}")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_people_outline_24),
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate("change_password")
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_passkey_24),
                            contentDescription = "Change Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = { authViewModel.signout(navController, context) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        AppScreen(
            backgroundColor = MaterialTheme.colorScheme.background,
            isPaddingNavigation = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                showNotifications -> {
                    if (notifications.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_notifications_off_24),
                                contentDescription = "No notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No notifications yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(
                                notifications,
                                key = {
                                    it["id"] as? String ?: it.hashCode().toString()
                                }) { notification ->
                                val type = notification["type"] as? String
                                val fromUserId = notification["fromUserId"] as? String
                                val read = notification["read"] as? Boolean ?: false
                                val notifId = notification["id"] as? String

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

                                    Box(Modifier.animateItemPlacement()) {
                                        LikeNotificationItem(
                                            likedUserName = if (likedUserName.isBlank()) "Someone" else likedUserName,
                                            read = read,
                                            onAgree = {
                                                val uid = currentUserId
                                                if (uid != null) {
                                                    handleLike(uid, fromUserId)
                                                    if (notifId != null) {
                                                        FirebaseFirestore.getInstance()
                                                            .collection("users").document(uid)
                                                            .collection("notifications")
                                                            .document(notifId)
                                                            .update("read", true)
                                                    }
                                                }
                                            },
                                            onIgnore = {
                                                val uid = currentUserId
                                                if (uid != null) {
                                                    handleIgnore(uid, fromUserId)
                                                    if (notifId != null) {
                                                        FirebaseFirestore.getInstance()
                                                            .collection("users").document(uid)
                                                            .collection("notifications")
                                                            .document(notifId)
                                                            .update("read", true)
                                                    }
                                                }
                                            },
                                            onClickMarkRead = {
                                                val uid = currentUserId
                                                if (!read && uid != null && notifId != null) {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("users").document(uid)
                                                        .collection("notifications")
                                                        .document(notifId)
                                                        .update("read", true)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                showLikedUsers -> {
                    if (likedUsers.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_favorite_24),
                                contentDescription = "No likes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No likes yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
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
                                        .clickable {
                                            val toUserId = like["toUserId"] as? String
                                            if (toUserId != null) {
                                                try {
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
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            if (likedUserImage != null) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        model = likedUserImage,
                                                        error = painterResource(id = R.drawable.defaultimg)
                                                    ),
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
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "View profile",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Image Section
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .clickable { pickImage() }
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
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

                            // Edit Icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { pickImage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_edit_24),
                                    contentDescription = "Edit profile",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (authState) {
                            is AuthState.Authenticated -> {
                                Text(
                                    text = userName.value,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Row {
                                    Icon(painter = painterResource(R.drawable.outline_location_on_24), contentDescription = "location")
                                    Text(
                                        text = if (ManagerLocation.isLocationUpdated) ManagerLocation.currentAddress else "Location not updated",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (ManagerLocation.isLocationUpdated) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }


                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            val shareText = "Hello, I'm ${userName.value}!"
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            val shareIntent =
                                                Intent.createChooser(sendIntent, "Share information")
                                            context.startActivity(shareIntent)
                                        }
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_share_24),
                                        contentDescription = "share",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Chia sẻ thông tin",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            is AuthState.Error -> {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Đang tải dữ liệu người dùng...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
    try {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = UUID.randomUUID().toString()
        val fileReference = storageReference.child("images/$fileName")

        fileReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseUpload", "Lỗi lấy URL: ${exception.message}", exception)
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUpload", "Tải ảnh lên thất bại: ${exception.message}", exception)
                onFailure(exception)
            }
            .addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount)
                Log.d("Upload", "Upload is $progress% done")
            }
    } catch (e: Exception) {
        Log.e("Upload", "Lỗi upload: ${e.message}", e)
        onFailure(e)
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

@Composable
fun LikeNotificationItem(
    likedUserName: String,
    read: Boolean,
    onAgree: () -> Unit,
    onIgnore: () -> Unit,
    onClickMarkRead: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize()
            .clickable { if (!read) onClickMarkRead() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (!read)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (!read) 4.dp else 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            headlineContent = {
                Text(
                    "$likedUserName liked your profile",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                if (!read) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(onClick = onAgree) { Text("Agree") }
                        OutlinedButton(onClick = onIgnore) { Text("Ignore") }
                    }
                } else {
                    Text("Already handled", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
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
