package com.example.projectdemo.pages.screen

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.projectdemo.lib.AppBox
import com.example.projectdemo.lib.AppText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun VideoCallScreen(
    navController: NavController,
    channelName: String
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var remoteUid by remember { mutableStateOf<Int?>(null) }

    val agoraManager = remember {
        AgoraManager(
            context = context,
            appId = "82e4075b34ac4b5cb43193009ca217c3", // Thay thế bằng App ID của bạn
            channelName = channelName,
            listener = object : AgoraManager.AgoraListener {
                override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                    Log.d("AgoraLog", "Tham gia kênh thành công: Channel=$channel, UID=$uid, Elapsed=$elapsed")
                    // Xử lý khi tham gia kênh thành công
                }

                override fun onUserJoined(uid: Int, elapsed: Int) {
                    Log.d("AgoraLog", "Người dùng khác đã tham gia: UID=$uid, Elapsed=$elapsed")
                    remoteUid = uid
                }

                override fun onUserOffline(uid: Int, reason: Int) {
                    Log.d("AgoraLog", "Người dùng rời khỏi: UID=$uid, Lý do=$reason")
                    remoteUid = null
                    navController.popBackStack()
                }

                override fun onError(err: Int) {
                    Log.e("AgoraLog", "Lỗi xảy ra: Mã lỗi=$err")
                    // Xử lý lỗi
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        agoraManager.initialize()
        agoraManager.joinChannel()
    }

    DisposableEffect(Unit) {
        onDispose {
            agoraManager.leaveChannel()
            agoraManager.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Remote video view
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    remoteUid?.let { uid ->
                        agoraManager.setupRemoteVideo(this, uid)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Local video view
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    agoraManager.setupLocalVideo(this)
                }
            },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    agoraManager.muteLocalAudio(isMuted)
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.Call else Icons.Default.Call,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    isVideoOff = !isVideoOff
                    agoraManager.muteLocalVideo(isVideoOff)
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isVideoOff) Icons.Default.Close else Icons.Default.Done,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    isFrontCamera = !isFrontCamera
                    agoraManager.switchCamera()
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    agoraManager.leaveChannel()
                    navController.popBackStack()
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "End Call",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
} 