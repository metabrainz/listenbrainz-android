package org.listenbrainz.android.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl

/** ColorScheme for the whole app. */
data class ColorScheme(
    val background: Color,
    val onBackground: Color,
    val nav: Color,
    val level1: Color,
    val level2: Color,
    val lbSignature: Color,
    val lbSignatureSecondary: Color,
    val lbSignatureInverse: Color,
    val onLbSignature: Color,
    val chipUnselected: Color,
    val chipSelected: Color,
    val dialogPositiveButtonEnabled: Color = Color(0xFF5DA855),
    val dialogPositiveButtonDisabled: Color = Color(0xFF9EB99C),
    val dialogNegativeButton: Color = Color(0xFF696658),
    val dialogNegativeButtonText: Color = Color.White,
    val text: Color,
    val listenText: Color,
    /** Used for stars.*/
    val golden: Color = Color(0xFFF9A825),
    val hint: Color
)

private val colorSchemeDark = ColorScheme(
    background = app_bg_dark,
    onBackground = Color.White,
    nav = bp_bottom_song_viewpager_dark,
    level1 = app_bottom_nav_dark,
    level2 = Color(0xFF4E4E4E),
    lbSignature = Color(0xFF9AABD1),
    lbSignatureSecondary = lb_yellow,
    lbSignatureInverse = lb_orange,
    onLbSignature = Color.Black,
    chipUnselected = Color(0xFF1E1E1E),
    chipSelected = Color.Black,
    text = Color.White,
    listenText = Color.White,
    hint = Color(0xFF8C8C8C)
)

private val colorSchemeLight = ColorScheme(
    background = app_bg_day,
    onBackground = Color.Black,
    nav = bp_bottom_song_viewpager_day,
    level1 = app_bottom_nav_day,
    level2 = Color(0xFF1E1E1E),
    lbSignature = lb_purple,
    lbSignatureSecondary = lb_yellow,
    lbSignatureInverse = Color(0xFFE5743E),
    onLbSignature = Color.White,
    chipUnselected = Color.White,
    chipSelected = Color(0xFFB6B6B6),
    text = Color.Black,
    listenText = lb_purple,
    hint = Color(0xFF707070)
)

private lateinit var LocalColorScheme: ProvidableCompositionLocal<ColorScheme>

private val DarkColorScheme = darkColorScheme(
    background = app_bg_dark,
    onBackground = app_bg_light,
    primary = app_bg_dark,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_dark,
    onTertiary = bp_color_primary_dark,
    inverseOnSurface = lb_orange,   // Reserved for progress indicators.

    surfaceTint = bp_lavender_dark,
    onSurface = Color.White,     // Text color (Which is ON surface/canvas)
)

private val LightColorScheme = lightColorScheme(
    background = app_bg_day,
    onBackground = app_bg_light,
    primary = app_bg_day,
    // Tertiary reserved for brainzPlayer's mini view
    tertiaryContainer = bp_bottom_song_viewpager_day,
    onTertiary = bp_color_primary_day,
    inverseOnSurface = lb_purple,   // Reserved for progress indicators.
    surfaceTint = bp_lavender_day,

    onSurface = Color.Black
)


@Immutable
data class Paddings(
    val defaultPadding: Dp = 16.dp,
    val tinyPadding: Dp = 4.dp,
    val smallPadding: Dp = 8.dp,
    val largePadding: Dp = 24.dp,
    
    // New set
    val horizontal: Dp = 9.dp,
    val vertical: Dp = 8.dp,
    val lazyListAdjacent: Dp = 6.dp,
    val coverArtAndTextGap: Dp = 8.dp,
    val insideCard: Dp = 8.dp,
    /** Padding for text inside custom made buttons.*/
    val insideButton: Dp = 8.dp,
    val adjacentDialogButtons: Dp = 8.dp,
    val chipsHorizontal: Dp = 6.dp,
    val insideDialog: Dp = 14.dp,
    val dialogContent: Dp = 8.dp
)
private val LocalPaddings = staticCompositionLocalOf { Paddings() }

@Immutable
data class Sizes(
    val listenCardHeight: Dp = 60.dp,
    val listenCardCorner: Dp = 8.dp,
    val dropdownItem: Dp = 20.dp
)

private val LocalSizes = staticCompositionLocalOf { Sizes() }

@Immutable
data class Shapes(
    // Change size field when changing this.
    val listenCardSmall: Shape = RoundedCornerShape(8.dp),
    val dialogs: Shape = RoundedCornerShape(4.dp),
    val listenCard: Shape = RoundedCornerShape(16.dp),
    val chips: Shape = RoundedCornerShape(4.dp)
)

private val LocalShapes = staticCompositionLocalOf { Shapes() }

@Immutable
data class TextStyles(
    val feedBlurbContent: TextStyle = TextStyle(fontStyle = FontStyle.Italic, fontSize = 15.sp),
    val feedBlurbContentTitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 14.sp),
    val chips: TextStyle = TextStyle(fontWeight = FontWeight.Medium),
    val dropdownItem: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val listenTitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold),
    val listenSubtitle: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
    
    // Dialog
    val dialogTitle: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 16.sp),
    val dialogTitleBold: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    val dialogButtonText: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val dialogText: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 14.sp),
    val dialogTextField: TextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 15.sp),
    val dialogTextBold: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

private val LocalTextStyles = staticCompositionLocalOf { TextStyles() }

private lateinit var LocalUiMode: ProvidableCompositionLocal<UiMode>

/** This function determines if the absolute UI mode of the app is dark (True) or not, irrespective of
 * what theme the device is using. Different from [isSystemInDarkTheme].*/
@Composable
fun onScreenUiModeIsDark() : Boolean {
    return when (LocalUiMode.current){
        UiMode.DARK -> true
        UiMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun ListenBrainzTheme(
    systemTheme: Boolean = isSystemInDarkTheme(),
    systemUiController: SystemUiController = rememberSystemUiController(),
    context: Context = LocalContext.current,
    appPreferences: AppPreferences = AppPreferencesImpl(context),
    // Dynamic color is available on Android 12+
    //dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    // dynamicColor: Boolean = false,//Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    
    val uiMode by appPreferences.themePreference.getFlow().collectAsState(initial = UiMode.FOLLOW_SYSTEM)
    LocalUiMode = staticCompositionLocalOf { uiMode }
    
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
    val colorScheme = when (uiMode) {
        UiMode.DARK -> DarkColorScheme
        UiMode.LIGHT -> LightColorScheme
        UiMode.FOLLOW_SYSTEM -> if (systemTheme) DarkColorScheme else LightColorScheme
    }
    
    // Custom ColorScheme
    val localColorScheme = remember(uiMode) {
        when (uiMode) {
            UiMode.DARK -> colorSchemeDark
            UiMode.LIGHT -> colorSchemeLight
            UiMode.FOLLOW_SYSTEM -> if (systemTheme) colorSchemeDark else colorSchemeLight
        }
    }
    
    LocalColorScheme = staticCompositionLocalOf { localColorScheme }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = localColorScheme.background.toArgb()
            val isDark = when (uiMode){
                UiMode.DARK -> false
                UiMode.LIGHT -> true
                UiMode.FOLLOW_SYSTEM -> !systemTheme
            }
            systemUiController.statusBarDarkContentEnabled = isDark
            systemUiController.navigationBarDarkContentEnabled = isDark
            systemUiController.setNavigationBarColor(color = colorScheme.tertiaryContainer)
        }
    }
    CompositionLocalProvider {
        LocalPaddings provides Paddings()
        LocalShapes provides Shapes()
        LocalSizes provides Sizes()
        LocalTextStyles provides TextStyles()
        LocalUiMode provides uiMode
        LocalColorScheme provides localColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


object ListenBrainzTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current
    
    val paddings: Paddings
        @Composable
        @ReadOnlyComposable
        get() = LocalPaddings.current
    
    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
    
    val sizes: Sizes
        @Composable
        @ReadOnlyComposable
        get() = LocalSizes.current
    
    val textStyles: TextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalTextStyles.current
    
    val uiModeIsDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() {
            return when (LocalUiMode.current){
                UiMode.DARK -> true
                UiMode.LIGHT -> false
                else -> isSystemInDarkTheme()
            }
        }
}
