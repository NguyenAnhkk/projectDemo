package com.example.projectdemo.feature.profile

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.example.projectdemo.lib.AppBox
import com.example.projectdemo.lib.AppColumn
import com.example.projectdemo.lib.AppRow
import com.example.projectdemo.lib.AppDimens
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase

data class CallModel(
    val callerId: String? = null,
    val receiverId: String? = null,
    val status: String? = null,
    val channelName: String? = null,
    val type: String? = null,
    val timestamp: Long? = null
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SimpleDateFormat")
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
    var showIncomingCallDialog by remember { mutableStateOf(false) }
    var incomingCallChannel by remember { mutableStateOf<String?>(null) }
    var callerId by remember { mutableStateOf<String?>(null) }
    var shouldStartCall by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId, userId) {
        if (currentUserId != null) {
            val channelName = "$userId-$currentUserId" // callerId-receiverId
            val callRef = FirebaseDatabase.getInstance().getReference("calls").child(channelName)
            callRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val call = snapshot.getValue(CallModel::class.java)
                    Log.d("VideoCall", "[Listen] onDataChange: $call, myUserId: $currentUserId")
                    if (call != null && call.receiverId == currentUserId && call.status == "ringing") {
                        showIncomingCallDialog = true
                        incomingCallChannel = channelName
                        callerId = call.callerId
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e("VideoCall", "[Listen] Firebase error: $error")
                }
            })
        }
    }
    if (showIncomingCallDialog && incomingCallChannel != null && callerId != null) {
        AlertDialog(
            onDismissRequest = { showIncomingCallDialog = false },
            title = { AppText("Cuộc gọi đến") },
            text = { AppText("Người dùng $callerId đang gọi cho bạn") },
            confirmButton = {
               Button(onClick = {
                    // Đồng ý: chuyển sang màn hình VideoCall với isIncomingCall=false
                    navController.navigate("video_call/$incomingCallChannel/$currentUserId/$userId?isIncomingCall=true")
                    showIncomingCallDialog = false
                }) {
                    AppText("Đồng ý")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // Từ chối: cập nhật status = rejected
                    FirebaseDatabase.getInstance().getReference("calls")
                        .child(incomingCallChannel!!)
                        .child("status")
                        .setValue("rejected")
                    showIncomingCallDialog = false
                }) {
                 Text("Từ chối")
                }
            }
        )
    }
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
                    AppRow(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFFFF)),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "Back",
                                tint = Color(0xFF0084FF)
                            )
                        }
                        AppBox(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF0084FF), CircleShape)
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
                        AppColumn {
                            AppTextBold(
                                text = userName.value,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            AppText(
                                text = "Active now",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFFFFFFF)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFF3D9))
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
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color(0xFF0084FF), CircleShape)
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = userName.value,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date of Birth: ${dateOfBirth.value}",
                                fontSize = 14.sp,
                                color = Color(0xFF65676B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    itemsIndexed(messages) { index, msg ->
                        var showTime by remember { mutableStateOf(false) }
                        val isFromReceiver = msg["senderId"] != currentUserId
                        val isPreviousMessageFromReceiver =
                            if (index > 0) messages[index - 1]["senderId"] == msg["senderId"] else false
                        val isNextMessageFromReceiver =
                            if (index < messages.size - 1) messages[index + 1]["senderId"] == msg["senderId"] else false

                        // Check time difference with previous message
                        val timeDiff = if (index > 0) {
                            val prevTime = messages[index - 1]["timestamp"] as? Long ?: 0L
                            val currentTime = msg["timestamp"] as? Long ?: 0L
                            (currentTime - prevTime) / (1000 * 60) // difference in minutes
                        } else {
                            Long.MAX_VALUE
                        }

                        // Show timestamp if time difference is more than 10 minutes
                        val shouldShowTimestamp = timeDiff > 10

                        // Reset showTime when a new message is selected
                        LaunchedEffect(selectedMessage) {
                            if (selectedMessage != msg) {
                                showTime = false
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (shouldShowTimestamp) {
                                val time = msg["timestamp"] as? Long
                                if (time != null) {
                                    val currentTime = System.currentTimeMillis()
                                    val messageTime = time
                                    val diffInMinutes = (currentTime - messageTime) / (1000 * 60)
                                    val diffInDays = (currentTime - messageTime) / (1000 * 60 * 60 * 24)

                                    val timeText = when {
                                        diffInMinutes < 1 -> "Vừa xong"
                                        diffInMinutes < 60 -> "$diffInMinutes phút trước"
                                        diffInDays < 1 -> java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                        diffInDays < 7 -> {
                                            val dayOfWeek = java.text.SimpleDateFormat("EEEE", java.util.Locale("vi")).format(messageTime)
                                            "$dayOfWeek lúc ${java.text.SimpleDateFormat("HH:mm").format(messageTime)}"
                                        }
                                        diffInDays < 365 -> java.text.SimpleDateFormat("dd/MM").format(messageTime) + " lúc " + java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                        else -> java.text.SimpleDateFormat("dd/MM/yyyy").format(messageTime) + " lúc " + java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                    }

                                    Text(
                                        text = timeText,
                                        color = Color(0xFF65676B),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
                            ) {
                                if (isFromReceiver && !isNextMessageFromReceiver) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFFE4E6EB), CircleShape)
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
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .padding(
                                            start = if (isFromReceiver && isNextMessageFromReceiver) 36.dp else 0.dp,
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    if (isFromReceiver) {
                                                        RoundedCornerShape(
                                                            topStart = 18.dp,
                                                            topEnd = 18.dp,
                                                            bottomStart = 5.dp,
                                                            bottomEnd = 18.dp
                                                        )
                                                    } else {
                                                        RoundedCornerShape(
                                                            topStart = 18.dp,
                                                            topEnd = 18.dp,
                                                            bottomStart = 18.dp,
                                                            bottomEnd = 5.dp
                                                        )
                                                    }
                                                )
                                                .background(
                                                    if (msg["senderId"] == currentUserId && msg["imageUrl"] == null) Color(
                                                        0xFF0084FF
                                                    )
                                                    else if (msg["imageUrl"] == null) Color.White
                                                    else Color.Transparent
                                                )
                                                .widthIn(
                                                    min = 0.dp,
                                                    max = (0.7f * LocalConfiguration.current.screenWidthDp).dp
                                                )
                                                .combinedClickable(
                                                    onClick = {
                                                        selectedMessage = msg
                                                        showTime = !showTime
                                                    },
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
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(300.dp)
                                                        .clip(
                                                            if (isFromReceiver) {
                                                                RoundedCornerShape(
                                                                    topStart = 18.dp,
                                                                    topEnd = 18.dp,
                                                                    bottomStart = 5.dp,
                                                                    bottomEnd = 18.dp
                                                                )
                                                            } else {
                                                                RoundedCornerShape(
                                                                    topStart = 18.dp,
                                                                    topEnd = 18.dp,
                                                                    bottomStart = 18.dp,
                                                                    bottomEnd = 5.dp
                                                                )
                                                            }
                                                        )
                                                        .clickable {
                                                            selectedImageUri = msg["imageUrl"] as String
                                                            isImageZoomed = true
                                                        }
                                                )
                                            } else {
                                                Text(
                                                    text = msg["message"] as String? ?: "",
                                                    color = if (msg["senderId"] == currentUserId) Color.White else Color.Black,
                                                    fontWeight = FontWeight.Normal,
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 8.dp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    if (showTime && selectedMessage == msg) {
                                        val time = msg["timestamp"] as? Long
                                        if (time != null) {
                                            val currentTime = System.currentTimeMillis()
                                            val messageTime = time
                                            val diffInMinutes = (currentTime - messageTime) / (1000 * 60)
                                            val diffInDays = (currentTime - messageTime) / (1000 * 60 * 60 * 24)

                                            val timeText = when {
                                                diffInMinutes < 1 -> "Vừa xong"
                                                diffInMinutes < 60 -> "$diffInMinutes phút trước"
                                                diffInDays < 1 -> java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                                diffInDays < 7 -> {
                                                    val dayOfWeek = java.text.SimpleDateFormat("EEEE", java.util.Locale("vi")).format(messageTime)
                                                    "$dayOfWeek lúc ${java.text.SimpleDateFormat("HH:mm").format(messageTime)}"
                                                }
                                                diffInDays < 365 -> java.text.SimpleDateFormat("dd/MM").format(messageTime) + " lúc " + java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                                else -> java.text.SimpleDateFormat("dd/MM/yyyy").format(messageTime) + " lúc " + java.text.SimpleDateFormat("HH:mm").format(messageTime)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (isFromReceiver) Arrangement.Start else Arrangement.End
                                            ) {
                                                Text(
                                                    text = timeText,
                                                    color = Color(0xFF65676B),
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AppBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFFFFF3D9))
            ) {
                val dimens = AppDimens()
                AppRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.paddingTiny),
                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingTiny),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var isKeyboardVisible by remember { mutableStateOf(false) }
                    var showHiddenIcons by remember { mutableStateOf(false) }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    if (!isKeyboardVisible) {
                        AppRow(
                            horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { /* Add action */ },
                                modifier = Modifier.size(dimens.sizeImage)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_add_circle_24),
                                    contentDescription = "Add",
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                            IconButton(
                                onClick = { getImageLauncher.launch("image/*") },
                                modifier = Modifier.size(dimens.sizeImage)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                                    contentDescription = "Camera",
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                            IconButton(
                                onClick = {
                                    Log.d("VideoCall", "Call button clicked")
                                    // Tạo channel name từ ID của 2 người dùng
                                    val channelName = "$currentUserId-$userId"
                                    // Lưu thông tin cuộc gọi vào Firebase
                                    val callData = hashMapOf(
                                        "type" to "video_call",
                                        "status" to "ringing",
                                        "timestamp" to System.currentTimeMillis(),
                                        "callerId" to currentUserId,
                                        "receiverId" to userId,
                                        "channelName" to channelName
                                    )
                                    FirebaseDatabase.getInstance().getReference("calls")
                                        .child(channelName)
                                        .setValue(callData)
                                        .addOnSuccessListener {
                                            Log.d("VideoCall", "Call room created successfully: $channelName")
                                            // Chuyển đến màn hình video call
                                            navController.navigate("video_call/$channelName/$currentUserId/$userId")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("VideoCall", "Failed to create call room: $channelName", e)
                                        }
                                },
                                modifier = Modifier.size(dimens.sizeImage)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_call_24),
                                    contentDescription = "Video Call",
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    } else {
                        if (message.isNotBlank()) {
                            if (showHiddenIcons) {
                                AppRow(
                                    horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { /* Add action */ },
                                        modifier = Modifier.size(dimens.sizeIconGreat)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_add_circle_24),
                                            contentDescription = "Add",
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                    IconButton(
                                        onClick = { getImageLauncher.launch("image/*") },
                                        modifier = Modifier.size(dimens.sizeIconGreat)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                                            contentDescription = "Camera",
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                            } else {
                                IconButton(
                                    onClick = { showHiddenIcons = true },
                                    modifier = Modifier.size(dimens.sizeIconGreat)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                                        contentDescription = "Forward",
                                        tint = Color(0xFF8B5CF6)
                                    )
                                }
                            }
                        } else {
                            AppRow(
                                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { /* Add action */ },
                                    modifier = Modifier.size(dimens.sizeIconGreat)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_add_circle_24),
                                        contentDescription = "Add",
                                        tint = Color(0xFF8B5CF6)
                                    )
                                }
                                IconButton(
                                    onClick = { getImageLauncher.launch("image/*") },
                                    modifier = Modifier.size(dimens.sizeIconGreat)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                                        contentDescription = "Camera",
                                        tint = Color(0xFF8B5CF6)
                                    )
                                }
                            }
                        }
                    }

                    AppBox(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = message,
                            onValueChange = { message = it },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.sizeEdittext)
                                .background(Color.White, RoundedCornerShape(dimens.sizeRadiusCircle))
                                .padding(horizontal = dimens.paddingMediumMore, vertical = 0.dp),
                            textStyle = TextStyle(
                                fontSize = dimens.fontSizeMedium,
                                color = Color.Black
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            decorationBox = { innerTextField ->
                                AppBox(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (message.isEmpty()) {
                                        Text(
                                            text = "Nhắn tin",
                                            fontSize = dimens.fontSizeMedium,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            cursorBrush = SolidColor(Color(0xFF8B5CF6))
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentUserId != null && message.isNotBlank()) {
                                sendMessage(currentUserId, userId, message)
                                message = ""
                            }
                        },
                        modifier = Modifier.size(dimens.sizeIconGreat)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_send_24),
                            contentDescription = "Send",
                            tint = Color(0xFF8B5CF6)
                        )
                    }
                }
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
                    val scale = remember { mutableStateOf(1f) }
                    val offsetX = remember { mutableStateOf(0f) }
                    val offsetY = remember { mutableStateOf(0f) }
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Color.Transparent)
                            .clickable {
                                isImageZoomed = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Zoomed Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = maxOf(.5f, minOf(3f, scale.value)),
                                    scaleY = maxOf(.5f, minOf(3f, scale.value)),
                                    translationX = offsetX.value,
                                    translationY = offsetY.value
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures { centroid, pan, zoom, _ ->
                                        scale.value *= zoom
                                        offsetX.value += pan.x
                                        offsetY.value += pan.y
                                    }
                                }
                        )
                    }
                }
            }
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent.copy(alpha = 0.7f))
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
