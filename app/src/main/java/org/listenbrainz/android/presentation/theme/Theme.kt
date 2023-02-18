package org.listenbrainz.android.presentation.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/** Theme for the whole app. */

private val DarkColorScheme = darkColorScheme(
    background = app_bg_dark,
    onBackground = app_bg_light,
    primary = app_bg_dark,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_dark,
    onTertiary = bp_color_primary_dark,
    inverseOnSurface = lb_orange,   // Reserved for progress indicators.
    
    onSurface = Color.White     // Text color (Which is ON surface/canvas)
)

private val LightColorScheme = lightColorScheme(
    background = app_bg_day,
    onBackground = app_bg_light,
    primary = app_bg_day,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_day,
    onTertiary = bp_color_primary_day,
    inverseOnSurface = lb_purple,   // Reserved for progress indicators.
    
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
    systemTheme: Boolean = isSystemInDarkTheme(),
    systemUiController: SystemUiController = rememberSystemUiController(),
    context: Context = LocalContext.current,
    // Dynamic color is available on Android 12+
    //dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    // dynamicColor: Boolean = false,//Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
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
            val isDark = when (isUiModeIsDark.value){
                true -> false
                false -> true
                else -> !systemTheme
            }
            systemUiController.statusBarDarkContentEnabled = isDark
            systemUiController.navigationBarDarkContentEnabled = isDark
            systemUiController.setNavigationBarColor(color = colorScheme.background)
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
