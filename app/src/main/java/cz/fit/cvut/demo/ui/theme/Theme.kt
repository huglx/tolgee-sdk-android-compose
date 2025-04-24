package cz.fit.cvut.demo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TolgeePink,
    onPrimary = TolgeeWhite,
    primaryContainer = TolgeeDeepBlue,
    onPrimaryContainer = TolgeeLightPink,
    secondary = TolgeeBlueAccent,
    onSecondary = TolgeeWhite,
    secondaryContainer = TolgeePurple,
    onSecondaryContainer = TolgeeWhite,
    tertiary = TolgeeLightPink,
    onTertiary = TolgeeNavyBlue,
    background = TolgeeNavyBlue,
    onBackground = TolgeeWhite,
    surface = TolgeeDeepBlue,
    onSurface = TolgeeWhite,
    surfaceVariant = TolgeePurple.copy(alpha = 0.6f),
    onSurfaceVariant = TolgeeWhite,
    error = TolgeePink,
    onError = TolgeeWhite,
    outline = TolgeeLightPink.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = TolgeePink,
    onPrimary = TolgeeWhite,
    primaryContainer = TolgeeLightPink.copy(alpha = 0.3f),
    onPrimaryContainer = TolgeeDeepBlue,
    secondary = TolgeeBlueAccent,
    onSecondary = TolgeeWhite,
    secondaryContainer = TolgeeBlueAccent.copy(alpha = 0.2f),
    onSecondaryContainer = TolgeeDeepBlue,
    tertiary = TolgeePurple,
    onTertiary = TolgeeWhite,
    background = Color(0xFFF7F8FA),  // Светлый фон
    onBackground = TolgeeNavyBlue,
    surface = TolgeeWhite,
    onSurface = TolgeeNavyBlue,
    surfaceVariant = TolgeeWhite,
    onSurfaceVariant = TolgeePurple,
    error = TolgeePink,
    onError = TolgeeWhite,
    outline = TolgeeBlueAccent.copy(alpha = 0.3f)
)

@Composable
fun TolgeeSdkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our custom colors
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
    
    // Update the system UI colors to match our theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}