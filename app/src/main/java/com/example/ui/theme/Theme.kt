package com.example.ui.theme

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
    primary = CyanLight,
    onPrimary = TechNavyDark,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = CyanGlow,
    secondary = CyanPrimary,
    onSecondary = Color.White,
    secondaryContainer = TechSurfaceElevatedDark,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = EmeraldStatusOk,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldStatusOk,
    error = RoseStatusCritical,
    onError = Color.White,
    errorContainer = RoseContainer,
    onErrorContainer = Color(0xFFFFD1D8),
    background = TechNavyDark,
    onBackground = DarkTextPrimary,
    surface = TechSurfaceDark,
    onSurface = DarkTextPrimary,
    surfaceVariant = TechSurfaceElevatedDark,
    onSurfaceVariant = DarkTextSecondary,
    outline = TechCardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = SlateTextPrimary,
    tertiary = EmeraldStatusOk,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = RoseStatusCritical,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF9F1239),
    background = SlateLightBackground,
    onBackground = SlateTextPrimary,
    surface = SlateLightSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our engineered high-tech palette by default
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
        typography = Typography,
        content = content
    )
}
