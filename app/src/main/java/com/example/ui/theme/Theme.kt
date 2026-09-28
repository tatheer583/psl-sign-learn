package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = IrssaPrimary,
  onPrimary = Color.White,
  primaryContainer = CardLavender,
  onPrimaryContainer = IrssaPrimaryVariant,
  secondary = IrssaSecondary,
  onSecondary = Color.White,
  secondaryContainer = CardPeach,
  onSecondaryContainer = Color(0xFF9E3600),
  tertiary = IrssaTertiary,
  onTertiary = Color.White,
  tertiaryContainer = CardMint,
  onTertiaryContainer = Color(0xFF00684B),
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = Color(0xFF4B4F69),
  outline = Color(0xFFDFE2EE)
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF9C8FFF),
  onPrimary = Color(0xFF190065),
  primaryContainer = Color(0xFF38299C),
  onPrimaryContainer = Color(0xFFE5DEFF),
  secondary = Color(0xFFFF9E7B),
  onSecondary = Color(0xFF551900),
  secondaryContainer = Color(0xFF7A2700),
  onSecondaryContainer = Color(0xFFFFDBCD),
  tertiary = Color(0xFF59E2BE),
  onTertiary = Color(0xFF003827),
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFC7CAE0),
  outline = Color(0xFF3A3E5E)
)

val IrssaShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(14.dp),
  medium = RoundedCornerShape(20.dp),
  large = RoundedCornerShape(28.dp),
  extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep branded vibrant palette by default for children's app
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

  MaterialTheme(
    colorScheme = colorScheme,
    shapes = IrssaShapes,
    typography = Typography,
    content = content
  )
}
