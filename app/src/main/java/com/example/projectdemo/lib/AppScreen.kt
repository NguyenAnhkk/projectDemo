package com.example.projectdemo.lib

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MyAppTheme.appColor.transparent,
    isPaddingSystem: Boolean = false,
    isPaddingNavigation: Boolean = false,
    isShowDialog: Boolean = false,
    onCloseDialog: () -> Unit = {},
    blurRadiusDp: Dp = MyAppTheme.appDimens.blurRadius,
    contentDialog: @Composable ColumnScope.() -> Unit = { Box {} },
    isBackCloseDialog: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Surface(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    keyboardController?.hide()
                })
            }, color = backgroundColor
    ) {
        val modifierContent = if (isPaddingSystem) {
            Modifier.windowInsetsPadding(WindowInsets.systemBars)
        } else if (isPaddingNavigation) {
            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        } else Modifier
        AppColumn(
            verticalArrangement = Arrangement.Top,
            modifier = modifierContent
                .fillMaxSize()
                .blur(if (isShowDialog) blurRadiusDp else 0.dp),
        ) {
            content()
        }
        if (isShowDialog) {
            AppColumn(
                Modifier
                    .fillMaxSize()
                    .background(color = MyAppTheme.appColor.backgroundLoading.copy(alpha = 0.66f))
                    .clickable(enabled = false) { }
            ) {
                contentDialog()
            }

            BackHandler {
                if (isBackCloseDialog) {
                    onCloseDialog()
                }
            }

        }
    }
}