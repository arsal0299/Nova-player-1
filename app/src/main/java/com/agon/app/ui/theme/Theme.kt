package com.agon.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = WhiteText,
    primaryContainer = PrimaryPurple.copy(alpha = 0.2f),
    onPrimaryContainer = WhiteText,
    secondary = SecondaryBlue,
    onSecondary = WhiteText,
    secondaryContainer = SecondaryBlue.copy(alpha = 0.2f),
    onSecondaryContainer = WhiteText,
    tertiary = AccentCyan,
    onTertiary = WhiteText,
    background = DarkBackground,
    onBackground = WhiteText,
    surface = DarkSurface,
    onSurface = WhiteText,
    surfaceVariant = CardBackground,
    onSurfaceVariant = SecondaryText,
    outline = DividerColor,
    error = ErrorRed,
    onError = WhiteText,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = WhiteText,
    primaryContainer = PrimaryPurple.copy(alpha = 0.15f),
    onPrimaryContainer = PrimaryPurple,
    secondary = SecondaryBlue,
    onSecondary = WhiteText,
    secondaryContainer = SecondaryBlue.copy(alpha = 0.15f),
    onSecondaryContainer = SecondaryBlue,
    tertiary = AccentCyan,
    onTertiary = WhiteText,
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    error = ErrorRed,
    onError = WhiteText,
)

@Composable
fun AgonAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
