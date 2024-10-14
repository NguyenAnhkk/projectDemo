package com.example.projectdemo.viewdata

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.projectdemo.R
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
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var radiusInMeters by rememberSaveable {
        mutableStateOf(sharedPreferences.getFloat("radiusInMeters", 10000f))
    }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val radiusOptions = listOf(1000f, 5000f, 10000f, 15000f, 20000f,50000f,100000f,200000f,6000000f)
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
        firestore.collection("location")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                nearbyUsers.clear()
                for (doc in snapshot.documents) {
                    val lat = doc.getDouble("latitude") ?: continue
                    val lng = doc.getDouble("longitude") ?: continue
                    val userId = doc.id
                    if (userId == currentUserId) continue

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
                                firestore.collection("chats").document("$userId-$currentUserId")
                                    .collection("messages")
                                    .addSnapshotListener { messagesSnapshot, messagesError ->
                                        if (messagesError != null || messagesSnapshot == null) return@addSnapshotListener
                                    }
                            }
                    }
                }
            }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(drawerContent = {
        ModalDrawerSheet {
            Text(text = "Chọn khoảng cách: ${"%.0f".format(radiusInMeters)} m")

            Button(onClick = { expanded = !expanded }) {
                Text(text = "Chọn khoảng cách")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                radiusOptions.forEach { radius ->
                    DropdownMenuItem(onClick = {
                        radiusInMeters = radius
                        saveRadius(radiusInMeters)
                        expanded = false
                    }) {
                        Text(text = "${"%.0f".format(radius)} m")
                    }
                }
            }

        }
    }, drawerState = drawerState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Menu" , color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu" , tint = Color.Black)
                        }
                    }, colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFFFFFFFF))
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn {
                    items(nearbyUsers) { user ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("user_detail/${user.userId}")
                                    },
                                    onLongClick = {
                                        isSheetOpen = true
                                        selectedUserId = user.userId
                                    }
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDE9)),
                        ) {
                            Text(
                                text = "${user.userName}",
                                color = Color(0xFF117a46),
                                style = TextStyle(fontSize = 20.sp),
                                modifier = Modifier.padding(5.dp)
                            )
                            Text(
                                text = "Date of birth: ${user.dateOfBirth}",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp),
                                modifier = Modifier.padding(5.dp)
                            )
                            Text(
                                text = "Cách bạn khoảng: ${"%.2f".format(user.distance / 1000)} km",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp),
                                modifier = Modifier.padding(5.dp)
                            )
                            Text(
                                text = "Địa chỉ: ${user.address}",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp),
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                }
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
    val senderChatRef = firestore.collection("chats").document("$senderId-$receiverId").collection("messages")
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