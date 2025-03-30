package com.example.projectdemo.lib

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFF04C3DF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val White = Color(0xFFFAFAFA)
val BackgroundLight = Color(0xFFFAFAFA)

val Purple40 = Color(0xFF327680)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Black = Color(0xFF1E1E1E)
val BackgroundDark = Color(0xFF0F1216)

fun getBaseColorLight() = AppColor(
    warning = Color(0xFFF44336),
    success = Color(0xFF2B882F),
    onWarning = Color(0xFFFCC0BC),
    textNormal = Black,
    textGray = Color(0xFF9C9C9C),
    textGrayLight = Color(0xFFC2C2C2),
    divider = Color(0x884D4D4D),
    primary = Purple80,

    primaryBlur = Purple80.copy(alpha = 0.4f),
    textButtonBackGround = Color(0xFF232427),
    backgroundTextField = Color(0xFFF0F0F0),

    backgroundLoading = Color(0xFFF0F0F0),
    backgroundItem = Color(0xFFF0F0F0),
    backgroundItemIn = Color(0xFFDFDDDD),

    background = BackgroundLight,

    )

fun getBaseColorDark() = AppColor(
    warning = Color(0xFFAD2C23),
    success = Color(0xFF2B882F),
    onWarning = Color(0xFFAF7875),
    textNormal = White,
    textGray = Color(0xFF858585),
    textGrayLight = Color(0xFFD8D8D8),
    divider = Color(0x884D4D4D),
    primary = Purple80,
    primaryBlur = Purple80.copy(alpha = 0.4f),
    textButtonBackGround = Color(0xFF232427),
    backgroundTextField = Color(0xFF302F2F),

    backgroundLoading = Color(0xFF535353),
    backgroundItem = Color(0xFF232427),
    backgroundItemIn = Color(0xFF303338),

    background = BackgroundDark,
)

open class AppColor(
    val warning: Color = Color.Unspecified,
    val success: Color = Color.Unspecified,
    val onWarning: Color = Color.Unspecified,
    val textNormal: Color = Color.Unspecified,
    val textGray: Color = Color.Unspecified,
    val textGrayLight: Color = Color.Unspecified,
    val unspecified: Color = Color.Unspecified,
    val divider: Color = Color.Unspecified,
    val textButtonBackGround: Color = Color.Unspecified,
    val primary: Color = Color.Unspecified,
    val primaryGradient: Color = Color.Unspecified,
    val primaryBlur: Color = Color.Unspecified,
    val transparent: Color = Color.Transparent,
    val clickIndication : Color = primaryBlur,
    val backgroundTextField : Color = Color.Unspecified,
    val backgroundLoading : Color = Color.Unspecified,

    val background: Color = Color.Unspecified,
    val backgroundItem: Color = Color.Unspecified,
    val backgroundItemIn: Color = Color.Unspecified,

    val textButtonWhite: Color = Color(0xFFF0F0F0),
    val dotYellow: Color = Color(0xFFFFA629),
    val dotBlue: Color = Color(0xFF31A8FF),
    val dotGreen: Color = Color(0xFF14AE5C),
    val dotRed: Color = Color(0xFFFF5A4D),
)



