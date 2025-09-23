package com.example.projectdemo.feature.matches

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.projectdemo.R
import com.example.projectdemo.lib.AppBox
import com.example.projectdemo.lib.AppRow
import com.example.projectdemo.lib.AppText
import com.example.projectdemo.lib.AppTextBold
import com.google.firebase.database.FirebaseDatabase
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.entity.*
import im.zego.zegoexpress.constants.ZegoPublisherState
import im.zego.zegoexpress.constants.ZegoPlayerState
import im.zego.zegoexpress.constants.ZegoRoomState
import android.widget.FrameLayout

import androidx.compose.ui.unit.sp
import com.example.projectdemo.feature.profile.CallModel
import com.example.projectdemo.utils.RequestVideoCallPermissions
import im.zego.zegoexpress.constants.ZegoUpdateType
import org.json.JSONObject
import java.util.ArrayList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

fun listenForIncomingCall(
    channelName: String,
    myUserId: String,
    onIncomingCall: (CallModel) -> Unit
) {
    val callRef =
        FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("calls")
            .child(channelName)

    callRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val call = snapshot.getValue(CallModel::class.java)
            Log.d("VideoCall", "onDataChange: $call, myUserId: $myUserId")
            if (call != null && call.receiverId == myUserId && call.status == "ringing") {
                Log.d("VideoCall", "Incoming call detected!")
                onIncomingCall(call)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("VideoCall", "Firebase error: $error")
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    navController: NavController,
    channelName: String,
    userId: String,
    receiverId: String,
    isIncomingCall: Boolean = false
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    // TODO: Thay thế bằng AppID và AppSign thực tế của bạn từ ZEGOCLOUD Console
    val appID: Long = 629861639
    val appSign = "2fb33d92cc70ed77181519a394dff5081ae6804fe8fcb59ac2cb8aeb0e77ee3b"
    var showIncomingCallDialog by remember { mutableStateOf(isIncomingCall) }
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var remoteUserID by remember { mutableStateOf<String?>(null) }
    var isCallConnected by remember { mutableStateOf(false) }
    var incomingCallerId by remember { mutableStateOf("") }
    var callStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var callTimeout = 30000L
    LaunchedEffect(channelName, userId) {
        try {
            if (isIncomingCall) {
                Log.d("VideoCall", "Setting up listener for incoming call")
                listenForIncomingCall(channelName, userId) { call ->
                    Log.d("VideoCall", "Incoming call received: $call")
                    incomingCallerId = call.callerId ?: ""
                    showIncomingCallDialog = true
                }
            } else {
                Log.d("VideoCall", "Making outgoing call")
                FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("calls")
                    .child(channelName)
                    .setValue(CallModel(userId, receiverId, "ringing"))
                    .addOnSuccessListener {
                        Log.d("VideoCall", "Call data written successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("VideoCall", "Error writing call data", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("VideoCall", "Error in call setup", e)
        }
    }
    RequestVideoCallPermissions(
        onPermissionsGranted = { permissionsGranted = true },
        onPermissionsDenied = {
            if (isIncomingCall) {
                FirebaseDatabase.getInstance().getReference("calls")
                    .child(channelName).child("status").setValue("rejected")
            }
            navController.popBackStack()
        }
    )

    val engine = remember(permissionsGranted, isIncomingCall, showIncomingCallDialog) {
        if (permissionsGranted && (!isIncomingCall || !showIncomingCallDialog)) {
            ZegoExpressEngine.createEngine(
                appID,
                appSign,
                false,
                ZegoScenario.GENERAL,
                context.applicationContext as Application,
                null
            )
        } else null
    }

    var localView by remember { mutableStateOf<FrameLayout?>(null) }

    LaunchedEffect(engine) {
        if (engine != null) {
            val frame = FrameLayout(context)
            engine.startPreview(ZegoCanvas(frame))
            localView = frame
        }
    }
    fun endCall() {
        engine?.let {
            it.stopPublishingStream()
            it.logoutRoom(channelName)
        }
        FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("calls")
            .child(channelName).setValue(
                CallModel(userId, receiverId, "ended")
            )
        navController.popBackStack()
    }
    LaunchedEffect(Unit) {
        if (!isIncomingCall) {
            while (System.currentTimeMillis() - callStartTime < callTimeout) {
                delay(1000)
            }
            if (!isCallConnected) {
                endCall()
            }
        }
    }

    LaunchedEffect(localView) {
        Log.d("CameraDebug", "LocalView updated: ${localView != null}")
    }
    LaunchedEffect(channelName, userId) {
        if (isIncomingCall) {
            listenForIncomingCall(channelName, userId) { call ->
                incomingCallerId = call.callerId ?: ""
                showIncomingCallDialog = true
            }
        } else {

            FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("calls")
                .child(channelName)
                .setValue(CallModel(userId, receiverId, "ringing"))
        }
    }
    var remoteView by remember { mutableStateOf<FrameLayout?>(null) }

    DisposableEffect(permissionsGranted) {
        if (!permissionsGranted || engine == null) {
            onDispose { }
        } else {
            engine.setEventHandler(object : IZegoEventHandler() {
                override fun onRoomStateUpdate(
                    roomID: String?,
                    state: ZegoRoomState?,
                    errorCode: Int,
                    extendedData: JSONObject?
                ) {
                    when (state) {
                        ZegoRoomState.CONNECTED -> {
                            Log.d("Zego", "Room connected successfully")
                            isCallConnected = true
                        }

                        ZegoRoomState.DISCONNECTED -> {
                            Log.d("Zego", "Room disconnected")
                            isCallConnected = false
                            remoteView = null
                        }

                        else -> {
                            Log.d("Zego", "Room state: $state")
                        }
                    }
                    if (errorCode != 0) {
                        Log.e("Zego", "Room join failed: $errorCode")
                    }
                }

                override fun onRoomUserUpdate(
                    roomID: String?,
                    updateType: ZegoUpdateType?,
                    userList: ArrayList<ZegoUser>?
                ) {
                    if (updateType == ZegoUpdateType.DELETE) {
                        remoteUserID = null
                    }
                }

                override fun onRoomStreamUpdate(
                    roomID: String?,
                    updateType: ZegoUpdateType?,
                    streamList: ArrayList<ZegoStream>?,
                    extendedData: JSONObject?
                ) {
                    if (updateType == ZegoUpdateType.ADD && !streamList.isNullOrEmpty()) {
                        val firstRemoteStreamId = streamList.first().streamID
                        Log.d("Zego", "Remote stream added: $firstRemoteStreamId")
                        remoteUserID = firstRemoteStreamId
                    }
                    if (updateType == ZegoUpdateType.DELETE) {
                        Log.d("Zego", "Remote stream removed")
                        remoteUserID = null
                    }
                }


                override fun onPublisherStateUpdate(
                    streamID: String,
                    state: ZegoPublisherState,
                    errorCode: Int,
                    extendedData: JSONObject?
                ) {
                    Log.d("Zego", "Publisher state: $state, error: $errorCode")
                }

                override fun onPlayerStateUpdate(
                    streamID: String,
                    state: ZegoPlayerState,
                    errorCode: Int,
                    extendedData: JSONObject?
                ) {
                    Log.d("Zego", "Player state: $state, error: $errorCode")
                }
            })

            val user = ZegoUser(userId)
            val roomConfig = ZegoRoomConfig()
            engine.loginRoom(channelName, user, roomConfig)
            val streamID = "stream_${userId}"
            engine.startPublishingStream(streamID)

            onDispose {
                engine.stopPublishingStream()
                engine.stopPreview()
                engine.logoutRoom(channelName)
                ZegoExpressEngine.destroyEngine(null)
            }
        }
    }

    // Lắng nghe remote stream
    LaunchedEffect(remoteUserID) {
        if (engine != null) {
            remoteUserID?.let { remoteID ->
                remoteView = FrameLayout(context).apply {
                    engine.startPlayingStream(remoteID, ZegoCanvas(this))
                }
            }
        }
    }
    if (showIncomingCallDialog) {
        IncomingCallDialog(
            callerId = incomingCallerId,
            onAccept = {
                showIncomingCallDialog = false
                // Start local preview and join room
                var activeEngine = engine
                if (activeEngine == null && permissionsGranted) {
                    activeEngine = ZegoExpressEngine.createEngine(
                        appID,
                        appSign,
                        false,
                        ZegoScenario.GENERAL,
                        context.applicationContext as Application,
                        null
                    )
                }
                val frame = FrameLayout(context)
                activeEngine?.startPreview(ZegoCanvas(frame))
                localView = frame

                val user = ZegoUser(userId)
                val roomConfig = ZegoRoomConfig()
                activeEngine?.loginRoom(channelName, user, roomConfig)
                activeEngine?.startPublishingStream(userId)

                // Update call status to "ongoing"
                FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("calls")
                    .child(channelName)
                    .child("status")
                    .setValue("ongoing")
            },
            onReject = {
                showIncomingCallDialog = false
                // Update call status to "rejected"
                FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("calls")
                    .child(channelName)
                    .child("status")
                    .setValue("rejected")
                    .addOnSuccessListener {
                        navController.popBackStack()
                    }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppTextBold(
                        text = if (isCallConnected) "Đang gọi..." else "Đang kết nối...",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            remoteView?.let { view ->
                AndroidView(
                    factory = { view },
                    modifier = Modifier.fillMaxSize()
                )
            }

            localView?.let { view ->
                AndroidView(
                    factory = { view },
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            AppRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                FloatingActionButton(
                    onClick = {
                        isMuted = !isMuted
                        engine?.muteMicrophone(isMuted)
                    },
                    containerColor = if (isMuted) Color.Red else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (isMuted) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_mic_off_24),
                            contentDescription = "Mic Off",
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_mic_24),
                            contentDescription = "Mic",
                        )
                    }

                }
                FloatingActionButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        engine?.useFrontCamera(isFrontCamera)
                    },
                    containerColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = {
                        isVideoOff = !isVideoOff
                        if (isVideoOff) {
                            engine?.stopPreview()
                        } else {
                            localView?.let { view ->
                                engine?.startPreview(ZegoCanvas(view))
                            }
                        }
                    },
                    containerColor = if (isVideoOff) Color.Red else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isVideoOff) R.drawable.baseline_videocam_off_24 else R.drawable.baseline_videocam_24),
                        contentDescription = "Video",
                    )
                }

                FloatingActionButton(
                    onClick = {
                        FirebaseDatabase.getInstance("https://projectdemo-def7a-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("calls")
                            .child(channelName)
                            .setValue(
                                mapOf(
                                    "callerId" to userId,
                                    "receiverId" to receiverId,
                                    "status" to "ended"
                                )
                            )
                            .addOnSuccessListener {
                                navController.popBackStack()
                            }
                    },
                    containerColor = Color.Red,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_call_end_24),
                        contentDescription = "End Call",
                        tint = Color.White
                    )
                }
            }

            if (remoteUserID == null) {
                AppBox(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    AppText(
                        text = "Đang chờ người dùng tham gia...",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun IncomingCallDialog(
    callerId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        title = { Text(text = "Cuộc gọi đến", style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = "Người dùng $callerId đang gọi cho bạn") },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Nghe")
            }
        },
        dismissButton = {
            Button(
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Từ chối")
            }
        }
    )
}