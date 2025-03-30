package com.example.projectdemo.lib

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.appOnClick(enabled: Boolean = true, onClick: () -> Unit,) =
    this.clickable(
        interactionSource = MutableInteractionSource(),
        indication = rememberRipple(bounded = false, color = MyAppTheme.appColor.clickIndication),
        enabled = enabled,
        onClick = onClick,
    )

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.appOnClickItem(onClick: () -> Unit) =
    this.clickable(
        interactionSource = MutableInteractionSource(),
        indication = rememberRipple(
            color = MyAppTheme.appColor.clickIndication,
        ),
        onClick = onClick,
    )

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.backgroundItem(
    color: Color = MyAppTheme.appColor.backgroundItem,
    radius: Dp = MyAppTheme.appDimens.sizeRadiusMedium
) = this.background(
    color = color,
    shape = RoundedCornerShape(
        radius,
    ),
)

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.backgroundItemGradient(
    colors: List<Color> = listOf(MyAppTheme.appColor.primary, MyAppTheme.appColor.primaryGradient),
    radius: Dp = MyAppTheme.appDimens.sizeRadiusMedium
) = this.background(
    brush = Brush.horizontalGradient(colors),
    shape = RoundedCornerShape(
        radius,
    ),
)

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.shadowItem(
    shadowColor: Color = MyAppTheme.appColor.backgroundItem,
    radius: Dp = MyAppTheme.appDimens.sizeRadiusMedium,
    elevation: Dp = MyAppTheme.appDimens.padding,
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = shadowColor.toArgb()
            setShadowLayer(radius.toPx(), 0f, 0f, shadowColor.toArgb())
        }
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height, elevation.toPx(), elevation.toPx(), paint
        )
    }
}

@Composable
fun Modifier.backgroundItem(
    color: Color = MyAppTheme.appColor.backgroundItem,

    topStartRadius: Dp = 0.dp,
    topEndRadius: Dp = 0.dp,
    bottomStartRadius: Dp = 0.dp,
    bottomEndRadius: Dp = 0.dp,
) = this.background(
    color = color,
    shape = RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius,
    ),
)

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.borderItem(
    color: Color = MyAppTheme.appColor.divider,
    radius: Dp = MyAppTheme.appDimens.sizeRadiusMedium,
) = this.border(
    width = MyAppTheme.appDimens.dividerThickness,
    shape = RoundedCornerShape(radius),
    color = color,
)

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
fun Modifier.imageRadius(
    radius: Dp = MyAppTheme.appDimens.sizeRadiusMedium
) = this.clip(
    shape = RoundedCornerShape(
        radius,
    ),
)