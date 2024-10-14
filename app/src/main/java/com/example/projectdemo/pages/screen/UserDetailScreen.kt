package com.example.projectdemo.pages.screen

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.projectdemo.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
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
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageZoomed by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    fun uploadImage(imageUri: Uri) {
        isLoading = true
        if (currentUserId != null) {
            val storageReference =
                FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}.jpg")
            storageReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        tempImageUri = null
                        sendImageMessage(currentUserId, userId, uri.toString())
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UploadImage", "Error uploading image", e)
                    isLoading = false
                }
        } else {
            Log.e("UploadImage", "User is not logged in")
        }
    }

    val getImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                tempImageUri = it
                uploadImage(it)
            }
        }
    var isFocused by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val textFieldWidth by animateDpAsState(targetValue = if (isFocused) screenWidth else screenWidth * 0.7f)
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
            isLoading = false
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error fetching profile data", e)
            isLoading = false
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

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(8.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFFFF)),
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "arrowPack", tint = Color.Black
                            )
                        }
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = if (imageUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageUrl),
                                        contentDescription = "User Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.defaultimg),
                                        contentDescription = "Default Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            ),
                            contentDescription = "User Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = "${userName.value}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFFFFFFFF))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                            Text(text = "Date of Birth: ${dateOfBirth.value}", color = Color.Black)
                        }
                    }

                    itemsIndexed(messages) { index, msg ->
                        var showTime by remember { mutableStateOf(false) }
                        val isFromReceiver = msg["senderId"] != currentUserId
                        val isPreviousMessageFromReceiver =
                            if (index > 0) messages[index - 1]["senderId"] == msg["senderId"] else false
                        val isNextMessageFromReceiver =
                            if (index < messages.size - 1) messages[index + 1]["senderId"] == msg["senderId"] else false

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
                        ) {
                            if (isFromReceiver && !isNextMessageFromReceiver) {
                                if (imageUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageUrl),
                                        contentDescription = "User Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.defaultimg),
                                        contentDescription = "Default Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(5.dp))
                            Column(
                                modifier = Modifier
                                    .padding(
                                        start = if (isFromReceiver && isNextMessageFromReceiver) 30.dp else 0.dp,
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (msg["senderId"] == currentUserId && msg["imageUrl"] == null) Color(
                                                    0xFF30b1fc
                                                )
                                                else if (msg["imageUrl"] == null) Color(0xFFF0F0F0)
                                                else Color.Transparent
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
                                            )
                                    ) {
                                        if (msg["imageUrl"] != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(model = msg["imageUrl"]),
                                                contentDescription = "Image Message",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .combinedClickable(
                                                        onClick = {
                                                            selectedImageUri = msg["imageUrl"] as String
                                                            isImageZoomed = true
                                                        },
                                                        onLongClick = {
                                                            selectedMessage = msg
                                                            showDialog = true
                                                        }
                                                    )
                                            )
                                        } else {
                                            Text(
                                                text = msg["message"] as String? ?: "",
                                                color = if (msg["senderId"] == currentUserId) Color.White else Color.Black,
                                                fontWeight = FontWeight.Normal,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = showTime) {
                                    val time = msg["timestamp"] as? Long
                                    if (time != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
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
                        .padding(10.dp)
                        .imePadding()
                        .width(textFieldWidth)
                        .animateContentSize()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    placeholder = { Text(text = "Tin nhắn", color = Color.Black) },
                    leadingIcon = {
                        IconButton(onClick = {
                            getImageLauncher.launch("image/*")
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                                contentDescription = "Thư viện ảnh",
                                modifier = Modifier.size(25.dp),
                                tint = Color(0xFF0084FF)
                            )
                        }
                    },
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
                                Modifier.size(25.dp), tint = Color(0xFF0084FF)
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.LightGray
                    ), singleLine = true
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

            if (isImageZoomed && selectedImageUri != null) {
                Dialog(onDismissRequest = { isImageZoomed = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable {
                                isImageZoomed = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Zoomed Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f))
                ) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animationloading))
                    LottieAnimation(
                        modifier = Modifier.size(150.dp),
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                }
            }
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

fun sendImageMessage(senderId: String, receiverId: String, imageUrl: String) {
    val firestore = FirebaseFirestore.getInstance()

    val newMessageRef =
        firestore.collection("chats").document("$senderId-$receiverId").collection("messages")
            .document()

    val messageId = newMessageRef.id

    val newMessage = hashMapOf(
        "senderId" to senderId,
        "receiverId" to receiverId,
        "imageUrl" to imageUrl,
        "timestamp" to System.currentTimeMillis(),
        "messageId" to messageId
    )
    newMessageRef.set(newMessage).addOnSuccessListener {
        Log.d("SendImageMessage", "Image message sent successfully")
    }.addOnFailureListener { e ->
        Log.e("SendImageMessage", "Error sending image message", e)
    }

    firestore.collection("chats").document("$receiverId-$senderId").collection("messages")
        .document(messageId).set(newMessage).addOnSuccessListener {
            Log.d("SendImageMessage", "Image message sent successfully to receiver's chat")
        }.addOnFailureListener { e ->
            Log.e("SendImageMessage", "Error sending image message to receiver's chat", e)
        }
}
