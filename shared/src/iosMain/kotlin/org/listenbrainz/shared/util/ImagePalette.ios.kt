package org.listenbrainz.shared.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

actual suspend fun getPaletteFromImage(bitmap: ImageBitmap): ImagePalette = withContext(Dispatchers.IO){
    val iosBitmap = bitmap.asSkiaBitmap()
    val width = iosBitmap.width
    val height = iosBitmap.height
    if(width <=0 || height <=0){
        return@withContext defaultPalette()
    }
    val colors = mutableListOf<Triple<Int,Int,Int>>()
    val points = listOf(
        Pair(width/4,height/4),
        Pair(width/2,height/2),
        Pair(width*3/4, height*3/4),
        Pair(width/4,height*3/4),
        Pair(width *3/4,height/4)
    )

    for(point in points){
        val colorValue = iosBitmap.getColor(point.first,point.second)
        val r = (colorValue shr 16) and 0xFF
        val g = (colorValue shr 8) and 0xFF
        val b = colorValue and 0xFF

        colors.add(Triple(r,g,b))
    }

    val sortByLuminance = colors.sortedBy { (0.299*it.first + 0.587*it.second + 0.114*it.third) }
    val darkestSample = sortByLuminance.first()
    val lightestSample = sortByLuminance.last()
    val mediumSample  = sortByLuminance[sortByLuminance.size/2]

    val lightColor = Color(mediumSample.first/ 255f,mediumSample.second/ 255f,mediumSample.third/ 255f)
    val darkColor = Color(darkestSample.first/ 255f,darkestSample.second/ 255f,darkestSample.third/ 255f)
    val textColorLight = Color(lightestSample.first/ 255f,lightestSample.second/ 255f,lightestSample.third/ 255f)

    ImagePalette(
        gradientColors = listOf(lightColor,darkColor),
        titleColorLight = textColorLight,
        bodyTextColorLight = Color.Black,
        titleTextColorDark = Color.White,
        bodyTextColorDark = Color.White,
        lightBacgroundColor = lightColor,
        darkBackgroundColor = darkColor
    )
}

private fun defaultPalette(): ImagePalette{
    val defaultColor = Color(0xFF888888)
    return ImagePalette(
        gradientColors = listOf(defaultColor,defaultColor),
        titleColorLight = Color.White,
        bodyTextColorLight = Color.Black,
        titleTextColorDark = Color.White,
        bodyTextColorDark = Color.White,
        lightBacgroundColor = defaultColor,
        darkBackgroundColor = defaultColor
    )
}