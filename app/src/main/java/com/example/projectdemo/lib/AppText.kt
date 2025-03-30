package com.example.projectdemo.lib

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun AppText(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = MyAppTheme.appColor.textNormal,
    textAlign: TextAlign = TextAlign.Center,
    fontSize: TextUnit = MyAppTheme.appDimens.fontSize,
    style: TextStyle = MyAppTheme.appTypography.labelNormal,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text?:"",
        modifier,
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        lineHeight = MyAppTheme.appDimens.lineHeight,
        letterSpacing = MyAppTheme.appDimens.letterSpacing,
        style = style,
        maxLines = maxLines,
        overflow = overflow
    )

}

@Composable
fun AppTextBold(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = MyAppTheme.appColor.textNormal,
    textAlign: TextAlign = TextAlign.Center,
    fontSize: TextUnit = MyAppTheme.appDimens.fontSize,
    style: TextStyle = MyAppTheme.appTypography.labelBold,
    maxLines: Int = Int.MAX_VALUE,
) {
    AppText(
        text,
        modifier,
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        style = style,
        maxLines = maxLines,
    )
}