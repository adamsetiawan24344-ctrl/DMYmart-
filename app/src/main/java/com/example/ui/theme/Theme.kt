package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = ShopeeOrange,
    onPrimary = Color.White,
    primaryContainer = ShopeeOrangeSoft,
    onPrimaryContainer = ShopeeOrangeDark,
    secondary = AmberFlash,
    onSecondary = Color.White,
    secondaryContainer = AmberSoft,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldVoucher,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSoft,
    onTertiaryContainer = Color(0xFF005B28),
    background = CanvasLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSoft,
    error = Color(0xFFD32F2F),
    onError = Color.White,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = ShopeeOrangeLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A190E),
    onPrimaryContainer = Color(0xFFFFD8CE),
    secondary = AmberFlash,
    onSecondary = Color(0xFF231900),
    secondaryContainer = Color(0xFF3E2D00),
    onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = EmeraldVoucher,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF003919),
    onTertiaryContainer = Color(0xFFA7F3C4),
    background = DarkBackground,
    onBackground = Color(0xFFF2F2F6),
    surface = DarkSurface,
    onSurface = Color(0xFFF2F2F6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB5B8C4),
    outline = Color(0xFF3A3D4A),
    error = Color(0xFFEF5350),
    onError = Color.White,
  )

val DMYmartShapes =
  Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun DMYmartTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = DMYmartTypography,
    shapes = DMYmartShapes,
    content = content,
  )
}
