package com.example.projectdemo.pages.screen

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.projectdemo.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: String,
) {
    val firestore = FirebaseFirestore.getInstance()
    val userName = remember { mutableStateOf("") }
    val dateOfBirth = remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var selectedMessage by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val sendSoundId = remember { soundPool.load(context, R.raw.buttonsound, 1) }

    LaunchedEffect(userId) {
        firestore.collection("profile").document(userId).get().addOnSuccessListener { document ->
            userName.value = document.getString("userName") ?: "N/A"
            dateOfBirth.value = document.getString("dateOfBirth") ?: "N/A"
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error fetching profile data", e)
        }

        firestore.collection("users").document(userId).collection("images")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    imageUrl = result.documents[0].getString("imageUrl")
                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching user images", e)
            }

        firestore.collection("chats").document("$currentUserId-$userId").collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { it.data }
                } else {
                    Log.e("FirestoreError", "Snapshot is null")
                }
            }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 0.dp, 0.dp, 75.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                reverseLayout = false
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            if (imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Name: ${userName.value}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(text = "Date of Birth: ${dateOfBirth.value}")
                    }
                }
                items(messages) { msg ->
                    var showTime by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = if (msg["senderId"] == currentUserId) Arrangement.End else Arrangement.Start
                    ) {
                        if (msg["receiverId"] == currentUserId) {
                            if (imageUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = "User Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.defaultimg),
                                    contentDescription = "Default Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg["senderId"] == currentUserId) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (msg["senderId"] == currentUserId) Color(
                                                0xFF30b1fc
                                            )
                                            else Color.Gray
                                        )
                                        .widthIn(
                                            min = 0.dp,
                                            max = (0.7f * LocalConfiguration.current.screenWidthDp).dp
                                        )
                                        .combinedClickable(
                                            onClick = { showTime = !showTime },
                                            onLongClick = {
                                                selectedMessage = msg
                                                showDialog = true
                                            }
                                        ),
                                ) {
                                    Text(
                                        text = msg["message"] as String? ?: "",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            AnimatedVisibility(visible = showTime) {
                                val time = msg["timestamp"] as? Long
                                if (time != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (msg["senderId"] == currentUserId) Arrangement.End else Arrangement.Start
                                    ) {
                                        Text(
                                            text = java.text.SimpleDateFormat("HH:mm  dd/MM/yyyy")
                                                .format(time),
                                            color = Color(0xFF40403f),
                                            fontSize = 10.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .imePadding(),
                placeholder = { Text(text = "Tin nhắn", color = Color.Black) },
                trailingIcon = {
                    IconButton(onClick = {
                        if (message.isNotBlank() && currentUserId != null) {
                            sendMessage(currentUserId, userId, message)
                            soundPool.play(sendSoundId, 1f, 1f, 0, 0, 1f)
                            message = ""
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "send Icon",
                            Modifier.size(25.dp)
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color.LightGray
                )
            )
        }

        if (showDialog && selectedMessage != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Tùy chọn tin nhắn") },
                text = { Text("Bạn muốn xóa tin nhắn này?") },
                confirmButton = {
                    Button(onClick = {
                        val messageId = selectedMessage?.get("messageId") as? String
                        if (messageId != null && selectedMessage?.get("senderId") == currentUserId) {
                            val senderId = selectedMessage?.get("senderId") as? String ?: ""
                            val receiverId = selectedMessage?.get("receiverId") as? String ?: ""
                            deleteMessage(senderId, receiverId, messageId) {
                                messages = messages.filterNot { it["messageId"] == messageId }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Bạn chỉ có thể xóa tin nhắn của mình.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        selectedMessage = null
                        showDialog = false
                    }) {
                        Text("Xóa")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDialog = false
                        selectedMessage = null
                    }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

fun sendMessage(senderId: String, receiverId: String, message: String) {
    val firestore = FirebaseFirestore.getInstance()

    val newMessageRef =
        firestore.collection("chats").document("$senderId-$receiverId").collection("messages")
            .document()

    val messageId = newMessageRef.id

    val newMessage = hashMapOf(
        "senderId" to senderId,
        "receiverId" to receiverId,
        "message" to message,
        "timestamp" to System.currentTimeMillis(),
        "messageId" to messageId
    )
    newMessageRef.set(newMessage).addOnSuccessListener {
        Log.d("SendMessage", "Message sent successfully to sender's chat")
    }.addOnFailureListener { e ->
        Log.e("SendMessage", "Error sending message to sender's chat", e)
    }
    firestore.collection("chats").document("$receiverId-$senderId").collection("messages")
        .document(messageId).set(newMessage).addOnSuccessListener {
            Log.d("SendMessage", "Message sent successfully to receiver's chat")
        }.addOnFailureListener { e ->
            Log.e("SendMessage", "Error sending message to receiver's chat", e)
        }
}

fun deleteMessage(senderId: String, receiverId: String, messageId: String, onComplete: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val senderChatRef = firestore.collection("chats").document("$senderId-$receiverId")
    senderChatRef.collection("messages").document(messageId).delete().addOnSuccessListener {
        Log.d("DeleteMessage", "Message deleted successfully from sender's chat")
    }.addOnFailureListener {
        Log.e("DeleteMessage", "Error deleting message from sender's chat", it)
    }

    val receiverChatRef = firestore.collection("chats").document("$receiverId-$senderId")
    receiverChatRef.collection("messages").document(messageId).delete().addOnSuccessListener {
        Log.d("DeleteMessage", "Message deleted successfully from receiver's chat")
        onComplete()
    }.addOnFailureListener {
        Log.e("DeleteMessage", "Error deleting message from receiver's chat", it)
    }
}
