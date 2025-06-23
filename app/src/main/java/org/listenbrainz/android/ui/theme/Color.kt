package org.listenbrainz.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * This File only contains those colors that are different in night and light modes.
 * TODO: Port all colors from colors.xml to this file.
 * */

/** BrainzPlayer Colors */
val bp_bottom_song_viewpager_day = Color(0xFFDFDFDF)
val bp_color_primary_day = Color(0xFF353070)
val bp_lavender_day = Color(0xFF2D2A31)

val bp_bottom_song_viewpager_dark = Color(0xFF030303)
val bp_color_primary_dark = Color(0xFFEA743B)
val bp_lavender_dark = Color(0xFFE9DCFE)

/** background Colors */
val app_bg_day = Color(0xFFFFFFFF)
val app_bg_dark = Color(0xFF292929)
val app_bg_secondary_dark = Color(0xFF1E1E1E)
val on_app_bg_dark = Color(0xFF101010)
val app_bg_secondary_light = Color(0xFFD9D9D9)
val app_bg_light = Color(0xFF8FA3AD)

//TODO: Change app_bg_light everywhere following approval from lead dev
val new_app_bg_light = Color(0xFFF5F5F5)
val app_bg_mid = Color(0xFF8D8D8D)
val app_bottom_nav_dark = Color(0xFF000000)
val app_bottom_nav_day = Color(0xFFFFFFFF)

val lb_orange = Color(0xFFEA743B)
val lb_purple = Color(0xFF353070)
val lb_yellow = Color(0xFFE59B2E)
val lb_purple_night = Color(0xFF9AABD1)

val yimYellow = Color(0xFFFECB49)
val yimRed = Color(0xFFFE0E25)
val yimWhite = Color(0xFFFEFEFE)
val offWhite = Color(0xFFF4F4F4)

val yim23Green = Color(0xFF4C6C52)
val yim23Background = Color(0xFFF0EEE2)
val yim23DarkGreen = Color(0xFF253127)
val yim23DarkRed = Color(0xFF8B3D40)
val yim23DarkBlue = Color(0xFF354F53)
val yim23DarkGray = Color(0xFF282423)
val yim23Red = Color(0xFFBE4A55)
val yim23Blue = Color(0xFF567276)
val yim23Grey = Color(0xFF4C4442)

/* User Page Colors */
val compatibilityMeterColor = Color(0xFFDB7544)

val onboardingGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF353070),
        Color(0xFF3D3956),
        Color(0xFF46433A)
    )
)