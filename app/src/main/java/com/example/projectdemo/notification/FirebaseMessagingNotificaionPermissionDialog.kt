package com.example.projectdemo.notification

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FirebaseMessagingNotificaionPermissionDialog(
    showNotificationDialog: MutableState<Boolean>,
    notificationPermissionState: PermissionState
) {
    if (showNotificationDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showNotificationDialog.value = false
                notificationPermissionState.launchPermissionRequest()
            },
            title = { Text(text = "Notificaion permission") },
            text = { Text(text = "Please allow this app to send you a notification") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationDialog.value = false
                    notificationPermissionState.launchPermissionRequest()
                    Firebase.messaging.subscribeToTopic("Tutorial")
                }) {
                    Text(text = "OK")
                }
            })
    }
}