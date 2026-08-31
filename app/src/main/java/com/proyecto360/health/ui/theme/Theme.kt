package com.proyecto360.health.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF1B4D3E)
private val Sage = Color(0xFF3D7A66)
private val Mist = Color(0xFFE8F5F0)
private val Sand = Color(0xFFF7F3EB)
private val Ink = Color(0xFF1A2A24)
private val SoftCoral = Color(0xFFC96B4B)

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Mist,
    onPrimaryContainer = Ink,
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5EBE2),
    onSecondaryContainer = Ink,
    tertiary = SoftCoral,
    onTertiary = Color.White,
    background = Sand,
    onBackground = Ink,
    surface = Color(0xFFFFFBF6),
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF3F524A),
    outline = Color(0xFF8AA399)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FCBB4),
    onPrimary = Color(0xFF003828),
    primaryContainer = Forest,
    onPrimaryContainer = Mist,
    secondary = Color(0xFFA5CFBF),
    onSecondary = Color(0xFF00382B),
    background = Color(0xFF101916),
    onBackground = Color(0xFFE4EDE8),
    surface = Color(0xFF16211D),
    onSurface = Color(0xFFE4EDE8),
    surfaceVariant = Color(0xFF2A3A34),
    onSurfaceVariant = Color(0xFFC2D3CB),
    tertiary = Color(0xFFE2A48E),
    onTertiary = Color(0xFF4A1F12)
)

@Composable
fun HealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
