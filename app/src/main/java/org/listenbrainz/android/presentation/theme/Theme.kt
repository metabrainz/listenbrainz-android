package org.listenbrainz.android.presentation.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceManager
import org.listenbrainz.android.presentation.UserPreferences

/** Theme for the whole app. */

private val DarkColorScheme = darkColorScheme(
    background = app_bg_night,
    onBackground = app_bg_light,
    primary = app_bg_night,
    onSurface = Color.White     // Text color (Which is ON surface/canvas)
)

private val LightColorScheme = lightColorScheme(
    background = app_bg_day,
    onBackground = app_bg_light,
    primary = app_bg_day,
    onSurface = Color.Black
)

// Paddings for out compose part
@Immutable
data class Paddings(
    val defaultPadding: Dp = 16.dp,
    val tinyPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val largePadding: Dp = 24.dp
)
internal val LocalPaddings = staticCompositionLocalOf { Paddings() }

@Composable
fun ListenBrainzTheme(
    darkTheme: Boolean? = userSelectedThemeIsNight(context = LocalContext.current),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (darkTheme) {
        /*dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }*/
        true -> DarkColorScheme
        false -> LightColorScheme
        else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =
                when (colorScheme) {
                    DarkColorScheme -> false
                    else -> true
                }
        }
    }
    CompositionLocalProvider {
        LocalPaddings provides Paddings()
    }
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// TODO: CHANGE THEME ONCLICK
/*var uiModeIsDark : Boolean? = when (UserPreferences.themePreference){
    "Dark" -> true      // Edit this variable in settings activity.
    "Light" -> false
    else -> null
}

internal val LocalUiModeCode = compositionLocalOf { uiModeIsDark }

@Composable
fun ListenBrainzTheme(
    darkTheme: Boolean? = LocalUiModeCode.current,
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider {
        LocalPaddings provides Paddings()
        LocalUiModeCode provides uiModeIsDark
    }
    val colorScheme = when (uiModeIsDark) {
        *//*dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }*//*
        true -> DarkColorScheme
        false -> LightColorScheme
        else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =
                when (colorScheme) {
                    DarkColorScheme -> false
                    else -> true
                }
        }
    }
    
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}*/


// We can access Custom defined values for our theme from this object.
object ListenBrainzThemeValues {
    val colorScheme: ColorScheme
        @Composable
        get() = when (userSelectedThemeIsNight(context = LocalContext.current)) {
            true -> DarkColorScheme
            false -> LightColorScheme
            else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        }
    
    // TODO: Can Introduce saved shapes and typography.
    
    // TODO: Can Integrate Padding Values from here.
    val paddings: Paddings
        @Composable
        get() = LocalPaddings.current
}


fun userSelectedThemeIsNight(context: Context) : Boolean? {
    return when (PreferenceManager.getDefaultSharedPreferences(context)
        .getString("app_theme", "Use device theme")){   // R.string.settings_device_theme_use_device_theme
        "Dark" -> true
        "Light" -> false
        else -> null
    }
}