package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ElegantDarkColorScheme = darkColorScheme(
  primary = ElegantAccent,
  onPrimary = ElegantAccentText,
  primaryContainer = ElegantPillBg,
  onPrimaryContainer = ElegantTextMain,
  secondary = ElegantButtonBg,
  onSecondary = ElegantTextMain,
  tertiary = ElegantPillBg,
  onTertiary = ElegantTextMain,
  background = ElegantBackground,
  onBackground = ElegantTextMain,
  surface = ElegantCardBackground,
  onSurface = ElegantTextMain,
  surfaceVariant = ElegantButtonBg,
  onSurfaceVariant = ElegantTextSub,
  outline = ElegantBorderColor,
  outlineVariant = ElegantBorderColor
)

private val DarkColorScheme = ElegantDarkColorScheme

private val LightColorScheme = ElegantDarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the elegant dark design
  dynamicColor: Boolean = false, // Disable dynamic colors to keep elegant dark design consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = ElegantDarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
