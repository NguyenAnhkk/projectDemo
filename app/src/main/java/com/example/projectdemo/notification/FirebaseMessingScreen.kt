package com.example.projectdemo.notification

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FirebaseMessagingScreen() {
    val showNotificationDialog = remember { mutableStateOf(false) }

    val permission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (showNotificationDialog.value) {
        FirebaseMessagingNotificationPermissionDialog(
            showNotificationDialog = showNotificationDialog,
            notificationPermissionState = permission
        )
    }

    LaunchedEffect(key1 = Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permission.status.isGranted) {
                Firebase.messaging.subscribeToTopic("Tutorial")
            } else {
                showNotificationDialog.value = true
            }
        } else {
            Firebase.messaging.subscribeToTopic("Tutorial")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello main screen", color = Color.White)
    }
}
