package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

expect suspend fun getPaletteFromImage(bitmap: ImageBitmap): ImagePalette