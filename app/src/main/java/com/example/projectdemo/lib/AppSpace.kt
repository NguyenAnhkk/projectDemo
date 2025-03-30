package com.example.projectdemo.lib

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun AppSpacerVertical(
    modifier: Modifier = Modifier,
    height: Dp = MyAppTheme.appDimens.padding,
) {
    Spacer(modifier = modifier.height(height))
}

@Composable
fun AppSpacerVerticalHalf(
    modifier: Modifier = Modifier,
    height: Dp = MyAppTheme.appDimens.padding / 2,
) {
    Spacer(modifier = modifier.height(height))
}

@Composable
fun AppSpacerHorizontal(
    modifier: Modifier = Modifier,
    width: Dp = MyAppTheme.appDimens.padding,
) {
    Spacer(modifier = modifier.width(width))
}

@Composable
fun AppSpacerHorizontalHalf(
    modifier: Modifier = Modifier,
    width: Dp = MyAppTheme.appDimens.padding / 2,
) {
    Spacer(modifier = modifier.width(width))
}

@Composable
fun AppSpacerStatusBar(modifier: Modifier = Modifier) {
    AppBox(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
    }
}