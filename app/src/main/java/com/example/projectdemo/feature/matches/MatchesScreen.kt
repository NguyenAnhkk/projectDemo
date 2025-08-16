package com.example.projectdemo.feature.matches

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppScreen
import com.example.projectdemo.lib.MyAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var matches by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }


    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("matches")
                .whereArrayContains("users", currentUserId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val matchesList = snapshot.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply {
                            put("id", doc.id)
                            put("lastMessageTime", 0L)
                            put("lastMessage", "")
                        }
                    }.toMutableList()
                    // Get the most recent message for each match
                    matchesList.forEach { match ->
                        val otherUserId = if (match["user1"] == currentUserId) {
                            match["user2"] as String
                        } else {
                            match["user1"] as String
                        }
                        firestore.collection("chats")
                            .document("$currentUserId-$otherUserId")
                            .collection("messages")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { messageSnapshot ->
                                if (!messageSnapshot.isEmpty) {
                                    val lastMessage = messageSnapshot.documents[0]
                                    match["lastMessageTime"] =
                                        lastMessage.getLong("timestamp") ?: 0L
                                    match["lastMessage"] =
                                        if (lastMessage.getString("imageUrl") != null) {
                                            "Đã gửi một hình ảnh"
                                        } else {
                                            lastMessage.getString("message") ?: ""
                                        }
                                }
                                // Sort the list after each update
                                val sortedList = matchesList.sortedByDescending {
                                    (it["lastMessageTime"] as? Long) ?: 0L
                                }
                                matches = sortedList

                            }
                    }
                }
        }
    }
    AppScreen(
        backgroundColor = MyAppTheme.appColor.background,
        isPaddingNavigation = true,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            TopAppBar(
                title = { Text("Tin nhắn") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(matches) { match ->
                    var matchedUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                    var matchedUserImage by remember { mutableStateOf<String?>(null) }

                    // Get the other user's ID from the match
                    val otherUserId = if (match["user1"] == currentUserId) {
                        match["user2"] as String
                    } else {
                        match["user1"] as String
                    }

                    // Load matched user's data
                    LaunchedEffect(otherUserId) {
                        val firestore = FirebaseFirestore.getInstance()
                        // Get user profile
                        firestore.collection("profile")
                            .document(otherUserId)
                            .get()
                            .addOnSuccessListener { doc ->
                                matchedUser = doc.data
                            }

                        // Get user image
                        firestore.collection("users")
                            .document(otherUserId)
                            .collection("images")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { result ->
                                if (!result.isEmpty) {
                                    matchedUserImage = result.documents[0].getString("imageUrl")
                                }
                            }

                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("user_detail/$otherUserId")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
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
                                if (matchedUserImage != null) {

                                    Image(
                                        painter = rememberAsyncImagePainter(model = matchedUserImage),

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
                            Column {
                                Text(
                                    text = matchedUser?.get("userName") as? String
                                        ?: "Unknown User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = match["lastMessage"] as? String ?: "Chưa có tin nhắn",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 