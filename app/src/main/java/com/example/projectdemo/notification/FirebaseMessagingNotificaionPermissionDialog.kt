package com.example.projectdemo.notification

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import com.example.projectdemo.lib.AppText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FirebaseMessagingNotificationPermissionDialog(
    showNotificationDialog: MutableState<Boolean>,
    notificationPermissionState: PermissionState
) {
    if (showNotificationDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showNotificationDialog.value = false
            },
            title = { AppText(text = "Notification Permission") },
            text = { AppText(text = "Please allow this app to send you notifications") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationDialog.value = false
                    notificationPermissionState.launchPermissionRequest()
                }) {
                    AppText(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNotificationDialog.value = false
                }) {
                    AppText(text = "Cancel")
                }
            }
        )
    }

    LaunchedEffect(notificationPermissionState.status.isGranted) {
        if (notificationPermissionState.status.isGranted) {
            Firebase.messaging.subscribeToTopic("Tutorial")
        }
    }
}