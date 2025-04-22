package com.example.projectdemo.viewdata

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppRow
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.ulti.DraggableCard
import com.example.projectdemo.ulti.MutitableAnimationCircleFilledCanvas
import com.example.projectdemo.ulti.verticalGradientBackground
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CourseDetailsActivity(
    navController: NavController,
    currentLocation: LatLng,
    dataViewModel: DataViewModel = viewModel(),
) {
    val context = LocalContext.current
    val nearbyUsers = remember { mutableStateListOf<User>() }
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var radiusInMeters by rememberSaveable {
        mutableStateOf(sharedPreferences.getFloat("radiusInMeters", 10000f))
    }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val radiusOptions =
        listOf(1000f, 5000f, 10000f, 15000f, 20000f, 50000f, 100000f, 200000f, 6000000f)
    var expanded by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var lastSenderId by remember { mutableStateOf<String?>(null) }
    fun saveRadius(radius: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat("radiusInMeters", radius)
        editor.apply()
    }
    LaunchedEffect(isSheetOpen) {
        if (isSheetOpen) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        // First get all likes and ignores to filter out users
        firestore.collection("likes")
            .whereEqualTo("fromUserId", currentUserId)
            .get()
            .addOnSuccessListener { likesSnapshot ->
                val likedUserIds = likesSnapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
                
                firestore.collection("ignored")
                    .whereEqualTo("fromUserId", currentUserId)
                    .get()
                    .addOnSuccessListener { ignoresSnapshot ->
                        val ignoredUserIds = ignoresSnapshot.documents.mapNotNull { it.getString("toUserId") }.toSet()
                        
                        // Then get location data and filter out liked/ignored users
                        firestore.collection("location")
                            .addSnapshotListener { snapshot, error ->
                                if (error != null || snapshot == null) return@addSnapshotListener

                                nearbyUsers.clear()
                                for (doc in snapshot.documents) {
                                    val lat = doc.getDouble("latitude") ?: continue
                                    val lng = doc.getDouble("longitude") ?: continue
                                    val userId = doc.id
                                    
                                    // Skip if it's current user or user has been liked/ignored
                                    if (userId == currentUserId || 
                                        likedUserIds.contains(userId) || 
                                        ignoredUserIds.contains(userId)) continue

                                    val userLocation = LatLng(lat, lng)
                                    val distance = calculateDistance(currentLocation, userLocation)
                                    if (isWithinRadius(currentLocation, userLocation, radiusInMeters)) {
                                        firestore.collection("profile").document(userId)
                                            .get()
                                            .addOnSuccessListener { userDoc ->
                                                val userName = userDoc.getString("userName") ?: "N/A"
                                                val dateOfBirth = userDoc.getString("dateOfBirth") ?: "N/A"
                                                val district = getDistrictFromLatLng(context, userLocation)

                                                val user = User(
                                                    userId,
                                                    userName,
                                                    dateOfBirth,
                                                    userLocation,
                                                    distance,
                                                    district
                                                )
                                                nearbyUsers.add(user)
                                            }
                                    }
                                }
                            }
                    }
            }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(drawerContent = {
//        ModalDrawerSheet {
//            Text(text = "Chọn khoảng cách: ${"%.0f".format(radiusInMeters)} m")
//
//            Button(onClick = { expanded = !expanded }) {
//                Text(text = "Chọn khoảng cách")
//            }
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false }
//            ) {
//                radiusOptions.forEach { radius ->
//                    DropdownMenuItem(onClick = {
//                        radiusInMeters = radius
//                        saveRadius(radiusInMeters)
//                        expanded = false
//                    }) {
//                        Text(text = "${"%.0f".format(radius)} m")
//                    }
//                }
//            }
//
//        }
    }, drawerState = drawerState) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.verticalGradientBackground(
                    listOf(
                        Color.White,
                        Color(0xFF6200EE).copy(alpha = 0.2f)
                    )
                )
            ) {
                DatingLoader(nearbyUsers = nearbyUsers)
            }

            if (isSheetOpen) {
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { isSheetOpen = false }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column {
                            Button(
                                onClick = {
                                    val senderId = currentUserId ?: return@Button
                                    val receiverId = selectedUserId ?: return@Button
                                    deleteAllMessagesBetweenUsers(
                                        senderId = senderId,
                                        receiverId = receiverId,
                                        onComplete = { isSheetOpen = false }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_delete_24),
                                        contentDescription = "delete"
                                    )
                                    Text(text = "Xóa cuộc trò chuyện")
                                }
                            }
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_block_24),
                                        contentDescription = "block"
                                    )
                                    Text(text = "Chặn người dùng")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatingLoader(modifier: Modifier = Modifier, nearbyUsers: List<User>) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = screenHeight - 200.dp
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    Surface(modifier = Modifier) {
        val purple = Color(0xFF6200EE)
        Box(
            modifier = Modifier.verticalGradientBackground(
                listOf(
                    Color.White,
                    purple.copy(alpha = 0.2f)
                )
            )
        ) {
            val listEmpty = remember { mutableStateOf(false) }
            nearbyUsers.forEachIndexed { index, user ->
                DraggableCard(
                    item = user, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                        .padding(
                            top = 16.dp + (index + 2).dp,
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        ), 
                    onSwiped = { _, swipedUser ->
                        if (nearbyUsers.isNotEmpty()) {
                            nearbyUsers.toMutableList().remove(swipedUser)
                            if (nearbyUsers.isEmpty()) {
                                listEmpty.value = true
                            }
                        }
                    }
                ) {
                    CardContent(
                        user = user,
                        onFavoriteClick = { userId ->
                            if (currentUserId != null) {
                                handleLike(currentUserId, userId)
                                nearbyUsers.toMutableList().remove(user)
                                if (nearbyUsers.isEmpty()) {
                                    listEmpty.value = true
                                }
                            }
                        },
                        onIgnoreClick = { userId ->
                            if (currentUserId != null) {
                                handleIgnore(currentUserId, userId)
                                nearbyUsers.toMutableList().remove(user)
                                if (nearbyUsers.isEmpty()) {
                                    listEmpty.value = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardContent(
    modifier: Modifier = Modifier, 
    user: User,
    onFavoriteClick: (String) -> Unit,
    onIgnoreClick: (String) -> Unit
) {
    val purple = Color(0xFF6200EE)
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()

    // Load user's image
    LaunchedEffect(user.userId) {
        firestore.collection("users")
            .document(user.userId)
            .collection("images")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    imageUrl = snapshot.documents[0].getString("imageUrl")
                }
            }

        // Load initial favorite status
        if (currentUserId != null) {
            firestore.collection("favorites")
                .document("$currentUserId-${user.userId}")
                .get()
                .addOnSuccessListener { document ->
                    isFavorite = document.exists()
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl ?: R.drawable.defaultimg
                ),
                contentScale = ContentScale.Crop,
                contentDescription = "User Image",
                modifier = Modifier.fillMaxSize()
            )
            
            // Favorites and Remove buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        if (currentUserId != null) {
                            isFavorite = !isFavorite
                            if (isFavorite) {
                                // Add to favorites
                                val favoriteData = hashMapOf(
                                    "timestamp" to System.currentTimeMillis(),
                                    "fromUserId" to currentUserId,
                                    "toUserId" to user.userId
                                )
                                firestore.collection("favorites")
                                    .document("$currentUserId-${user.userId}")
                                    .set(favoriteData)
                                    .addOnSuccessListener {
                                        Log.d("Favorite", "Added to favorites successfully")
                                        onFavoriteClick(user.userId)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Favorite", "Error adding to favorites", e)
                                        isFavorite = false // Revert on failure
                                    }
                            } else {
                                // Remove from favorites
                                firestore.collection("favorites")
                                    .document("$currentUserId-${user.userId}")
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("Favorite", "Removed from favorites successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Favorite", "Error removing from favorites", e)
                                        isFavorite = true // Revert on failure
                                    }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                IconButton(
                    onClick = { onIgnoreClick(user.userId) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AppRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppColumn {
                    AppText(
                        text = user.userName,
                    )
                    AppText(
                        text = user.dateOfBirth,
                    )
                }
                AppColumn {
                    AppText(
                        text = user.address,
                    )
                }
            }
        }
    }
}

data class User(
    val userId: String,
    val userName: String,
    val dateOfBirth: String,
    val location: LatLng,
    val distance: Float,
    val address: String,
)

fun isWithinRadius(
    currentLocation: LatLng,
    userLocation: LatLng,
    radiusInMeters: Float
): Boolean {
    val currentLoc = Location("").apply {
        latitude = currentLocation.latitude
        longitude = currentLocation.longitude
    }
    val userLoc = Location("").apply {
        latitude = userLocation.latitude
        longitude = userLocation.longitude
    }
    return currentLoc.distanceTo(userLoc) <= radiusInMeters
}

fun calculateDistance(currentLocation: LatLng, userLocation: LatLng): Float {
    val currentLoc = Location("").apply {
        latitude = currentLocation.latitude
        longitude = currentLocation.longitude
    }
    val userLoc = Location("").apply {
        latitude = userLocation.latitude
        longitude = userLocation.longitude
    }
    return currentLoc.distanceTo(userLoc)
}

fun getDistrictFromLatLng(context: Context, latLng: LatLng): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    return if (addresses.isNullOrEmpty()) {
        "Không xác định"
    } else {
        addresses[0].subAdminArea ?: "Không xác định"
    }
}

fun deleteAllMessagesBetweenUsers(senderId: String, receiverId: String, onComplete: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val senderChatRef =
        firestore.collection("chats").document("$senderId-$receiverId").collection("messages")
    senderChatRef.get().addOnSuccessListener { documents ->
        for (document in documents) {
            document.reference.delete()
        }
        Log.d("DeleteMessages", "All messages deleted successfully from sender's chat")
        onComplete()
    }.addOnFailureListener { e ->
        Log.e("DeleteMessages", "Error deleting messages from sender's chat", e)
    }
}

fun updateLastReadTime(userId: String, chatPartnerId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val userRef = firestore.collection("users").document(userId)

    userRef.update("lastReadTime.$chatPartnerId", System.currentTimeMillis())
        .addOnSuccessListener {
            Log.d("UpdateLastReadTime", "Last read time updated successfully")
        }
        .addOnFailureListener { e ->
            Log.e("UpdateLastReadTime", "Error updating last read time", e)
        }
}

fun handleLike(currentUserId: String, likedUserId: String) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("likes")
        .document("$likedUserId-$currentUserId")
        .get()
        .addOnSuccessListener { otherUserLikeDoc ->
            if (otherUserLikeDoc.exists()) {
                createMatch(currentUserId, likedUserId)
                firestore.collection("likes")
                    .document("$likedUserId-$currentUserId")
                    .delete()
            }

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

                    if (!otherUserLikeDoc.exists()) {
                        Log.d("Like", "Like recorded successfully")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Like", "Error recording like", e)
                }
        }
        .addOnFailureListener { e ->
            Log.e("Like", "Error checking for mutual like", e)
        }
}

fun createMatch(userId1: String, userId2: String) {
    val firestore = FirebaseFirestore.getInstance()
    val matchId = if (userId1 < userId2) "$userId1-$userId2" else "$userId2-$userId1"

    val matchData = hashMapOf(
        "users" to listOf(userId1, userId2),
        "timestamp" to System.currentTimeMillis(),
        "user1" to userId1,
        "user2" to userId2
    )

    firestore.collection("matches")
        .document(matchId)
        .set(matchData)
        .addOnSuccessListener {
            Log.d("Match", "Match created successfully")

            // Create match notifications for both users
            createMatchNotification(userId1, userId2)
            createMatchNotification(userId2, userId1)
        }
        .addOnFailureListener { e ->
            Log.e("Match", "Error creating match", e)
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

private fun createMatchNotification(userId: String, matchedWithUserId: String) {
    val firestore = FirebaseFirestore.getInstance()

    val notificationData = hashMapOf(
        "type" to "match",
        "matchedWithUserId" to matchedWithUserId,
        "timestamp" to System.currentTimeMillis(),
        "read" to false
    )

    firestore.collection("users")
        .document(userId)
        .collection("notifications")
        .add(notificationData)
        .addOnSuccessListener {
            Log.d("MatchNotification", "Match notification created successfully")
        }
        .addOnFailureListener { e ->
            Log.e("MatchNotification", "Error creating match notification", e)
        }
}

fun handleIgnore(currentUserId: String, ignoredUserId: String) {
    val firestore = FirebaseFirestore.getInstance()
    
    // Add to ignored collection
    val ignoreData = hashMapOf(
        "timestamp" to System.currentTimeMillis(),
        "fromUserId" to currentUserId,
        "toUserId" to ignoredUserId
    )
    
    firestore.collection("ignored")
        .document("$currentUserId-$ignoredUserId")
        .set(ignoreData)
        .addOnSuccessListener {
            Log.d("Ignore", "User ignored successfully")
        }
        .addOnFailureListener { e ->
            Log.e("Ignore", "Error ignoring user", e)
        }
}