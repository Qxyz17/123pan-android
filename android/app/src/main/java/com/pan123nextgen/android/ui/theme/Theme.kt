package com.pan123nextgen.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// WinUI-inspired color palette
object WinUIColor {
    val Blue = Color(0xFF0078D4)
    val BlueDark = Color(0xFF1A73E8)
    val BlueLight = Color(0xFF4BA3E0)
    val BlueContainer = Color(0xFFDEEBF6)

    val SurfaceLight = Color(0xFFF2F2F2)
    val SurfaceDark = Color(0xFF1F1F1F)
    val CardLight = Color(0xFFFFFFFF)
    val CardDark = Color(0xFF2D2D2D)

    val TextPrimaryLight = Color(0xFF000000)
    val TextPrimaryDark = Color(0xFFFFFFFF)
    val TextSecondaryLight = Color(0xFF616161)
    val TextSecondaryDark = Color(0xFFABABAB)

    val DividerLight = Color(0xFFE0E0E0)
    val DividerDark = Color(0xFF3D3D3D)

    val Success = Color(0xFF107C10)
    val Warning = Color(0xFFAC4B1C)
    val Error = Color(0xFFD32F2F)

    // NavigationView-inspired
    val NavSelectedBg = Color(0xFFDEEBF6)
    val NavHoverBg = Color(0xFFE5E5E5)
    val NavDarkSelectedBg = Color(0xFF3B3B3B)
    val NavDarkHoverBg = Color(0xFF353535)
}

private val WinUILightColorScheme = lightColorScheme(
    primary = WinUIColor.Blue,
    onPrimary = Color.White,
    primaryContainer = WinUIColor.BlueContainer,
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF5A5D72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDEE1F8),
    onSecondaryContainer = Color(0xFF171A2C),
    tertiary = Color(0xFF006B5E),
    onTertiary = Color.White,
    background = WinUIColor.SurfaceLight,
    onBackground = WinUIColor.TextPrimaryLight,
    surface = WinUIColor.CardLight,
    onSurface = WinUIColor.TextPrimaryLight,
    surfaceVariant = Color(0xFFE2E2E2),
    onSurfaceVariant = WinUIColor.TextSecondaryLight,
    outline = WinUIColor.DividerLight,
    outlineVariant = WinUIColor.DividerLight,
    error = WinUIColor.Error,
    onError = Color.White,
)

private val WinUIDarkColorScheme = darkColorScheme(
    primary = WinUIColor.BlueLight,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = WinUIColor.BlueContainer,
    secondary = Color(0xFFC2C5DC),
    onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF434659),
    onSecondaryContainer = WinUIColor.BlueContainer,
    tertiary = Color(0xFF4CDAC8),
    onTertiary = Color(0xFF003830),
    background = WinUIColor.SurfaceDark,
    onBackground = WinUIColor.TextPrimaryDark,
    surface = WinUIColor.CardDark,
    onSurface = WinUIColor.TextPrimaryDark,
    surfaceVariant = Color(0xFF444444),
    onSurfaceVariant = WinUIColor.TextSecondaryDark,
    outline = WinUIColor.DividerDark,
    outlineVariant = WinUIColor.DividerDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun Pan123NextGenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WinUIDarkColorScheme else WinUILightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}