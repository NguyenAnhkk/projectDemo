package com.example.projectdemo.lib
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class AppDimens(
    val paddingSmall: Dp = 4.dp,
    val paddingTiny: Dp = 8.dp,
    val paddingMedium: Dp = 10.dp,
    val paddingMediumMore: Dp = 12.dp,
    val padding: Dp = 16.dp,
    val paddingMore: Dp = 24.dp,
    val paddingDouble: Dp = 32.dp,
    val paddingGreat: Dp = 48.dp,
    val paddingLarge: Dp = 64.dp,

    val dividerThickness: Dp = 0.5.dp,

    val sizeIconTiny: Dp = 12.dp,
    val sizeIconSmall: Dp = 16.dp,
    val sizeIcon: Dp = 24.dp,
    val sizeIconGreat: Dp = 28.dp,
    val sizeImage: Dp = 32.dp,
    val sizeEdittext: Dp = 42.dp,
    val sizeImageMedium: Dp = 64.dp,

    val sizeRadiusTiny: Dp = 6.dp,
    val sizeRadius: Dp = 12.dp,
    val sizeRadiusMedium: Dp = 13.dp,
    val sizeRadiusMore: Dp = 14.dp,
    val sizeRadiusGreat: Dp = 16.dp,
    val sizeRadiusCircle: Dp = 256.dp,

    val blurRadius: Dp = 16.dp,
    val blurRadiusBig: Dp = blurRadius*5,
    val sizeLoading: Dp = 12.dp,

    val sizeStatusBar: Dp = 48.dp,


    //todo Sp
    val lineHeight: TextUnit = 16.sp,
    val letterSpacing: TextUnit = 0.5.sp,

    val fontSizeSmall: TextUnit = 8.sp,
    val fontSizeTiny: TextUnit = 10.sp,
    val fontSize: TextUnit = 12.sp,
    val fontSizeMedium: TextUnit = 14.sp,
    val fontSizeGreat: TextUnit = 16.sp,
    val fontSizeBar: TextUnit = 20.sp,
    val fontSizeMax: TextUnit = 32.sp,
)
