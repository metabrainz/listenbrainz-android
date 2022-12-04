package org.listenbrainz.android.presentation.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceManager

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

// Padding suggestions for out compose part
@Immutable
data class Paddings(
    val defaultPadding: Dp = 16.dp,
    val tinyPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val largePadding: Dp = 24.dp
)
internal val LocalPaddings = staticCompositionLocalOf { Paddings() }

/**
 * This variable defines the ui mode of the system.
 *
 * If Value is
 *
 *            TRUE -> Selected Ui Mode is Dark
 *
 *            FALSE -> Selected Ui Mode is Light
 *
 *            NULL -> Selected Ui Mode is System Theme
 *
 * This variable is public because it is used in system settings */
lateinit var isUiModeIsDark : MutableState<Boolean?>

@Composable
fun ListenBrainzTheme(
    context: Context = LocalContext.current,
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = false,//Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val systemTheme = isSystemInDarkTheme()
    isUiModeIsDark = remember { mutableStateOf(userSelectedThemeIsNight(context)) }
    // With Dynamic Color
    /*val colorScheme = if (dynamicColor){
            when(isUiModeIsDark.value){
                true -> dynamicDarkColorScheme(context)
                false -> dynamicLightColorScheme(context)
                else -> if (systemTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            }else{
                when (isUiModeIsDark.value) {
                    true -> DarkColorScheme
                    false -> LightColorScheme
                    else -> if (systemTheme) DarkColorScheme else LightColorScheme
            }
    }*/
    // Without Dynamic Color
    val colorScheme = when (isUiModeIsDark.value) {
        true -> DarkColorScheme
        false -> LightColorScheme
        else -> if (systemTheme) DarkColorScheme else LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.background.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =
                when (isUiModeIsDark.value){
                    true -> false
                    false -> true
                    else -> !systemTheme
                }
                
        }
    }
    CompositionLocalProvider {
        LocalPaddings provides Paddings()
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

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
