package org.listenbrainz.android.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ImagePalette(
    val gradientColors: List<Color>,
    val titleColorLight: Color,
    val bodyTextColorLight: Color,
    val titleTextColorDark: Color,
    val bodyTextColorDark: Color,
    val darkBackgroundColor: Color,
    val lightBacgroundColor: Color
)

suspend fun getPaletteFromImage(bitmap: Bitmap): ImagePalette {
    return withContext(Dispatchers.IO) {
        val palette = Palette.from(bitmap).generate()
        val lightColor =
            palette.vibrantSwatch?.rgb ?: palette.mutedSwatch?.rgb ?: 0xFF888888.toInt()
        val darkColor = palette.darkVibrantSwatch?.rgb ?: palette.darkMutedSwatch?.rgb ?: lightColor
        val textColorDark =
            palette.darkVibrantSwatch?.titleTextColor ?: palette.darkMutedSwatch?.titleTextColor
            ?: 0xFFFFFFFF.toInt()
        val bodyTextColorDark =
            palette.darkVibrantSwatch?.bodyTextColor ?: palette.darkMutedSwatch?.bodyTextColor
            ?: 0xFF000000.toInt()
        val textColorLight =
            palette.lightVibrantSwatch?.titleTextColor ?: palette.lightMutedSwatch?.titleTextColor
            ?: 0xFFFFFFFF.toInt()
        val bodyTextColorLight =
            palette.lightVibrantSwatch?.bodyTextColor ?: palette.lightMutedSwatch?.bodyTextColor
            ?: 0xFF000000.toInt()
        ImagePalette(
            gradientColors = listOf(
                Color(lightColor),
                Color(darkColor)
            ),
            titleColorLight = Color(textColorLight),
            bodyTextColorLight = Color(bodyTextColorLight),
            titleTextColorDark = Color(textColorDark),
            bodyTextColorDark = Color(bodyTextColorDark),
            lightBacgroundColor = Color(lightColor),
            darkBackgroundColor = Color(darkColor)
        )
    }
}