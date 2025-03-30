package com.example.projectdemo.lib

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    appColorDark: AppColor = getBaseColorDark(),
    appColorLight: AppColor = getBaseColorLight(),
    appDimens: AppDimens = AppDimens(),
    appTypography: AppTypography = AppTypography(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val appColor = if (darkTheme) appColorDark else appColorLight
    CompositionLocalProvider(
        localAppColor provides appColor,
        localAppDimens provides appDimens,
        localAppTypography provides appTypography,
    ) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = colorScheme.background.copy(0.01f)
        )
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

internal val localAppColor = staticCompositionLocalOf {
    AppColor()
}
internal val localAppDimens = staticCompositionLocalOf {
    AppDimens()
}

internal val localAppTypography = staticCompositionLocalOf {
    AppTypography()
}
object MyAppTheme {
    val appColor: AppColor
        @Composable get() = localAppColor.current
    val appDimens: AppDimens
        @Composable get() = localAppDimens.current
    val appTypography: AppTypography
        @Composable get() = localAppTypography.current

    val  colorScheme  @Composable get() =  MaterialTheme.colorScheme
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    background = BackgroundDark,
    surface = BackgroundDark,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = BackgroundLight,
    onSurface = BackgroundLight,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = BackgroundLight,
    surface = BackgroundLight,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = BackgroundDark,
    onSurface = BackgroundDark,
)