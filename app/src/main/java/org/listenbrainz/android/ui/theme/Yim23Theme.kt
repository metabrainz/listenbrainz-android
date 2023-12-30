package org.listenbrainz.android.ui.theme

import android.app.Activity
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val greenColorScheme = darkColorScheme(
    background = yim23Background,
    onBackground = yim23Green,
    surface = yim23DarkGreen
)

private val redColorScheme = darkColorScheme(
    background = yim23Background,
    onBackground = yim23Red,
    surface = yim23DarkRed
)

private val blueColorScheme = darkColorScheme(
    background = yim23Background,
    onBackground = yim23Blue,
    surface = yim23DarkBlue
)

private val greyColorScheme = darkColorScheme(
    background = yim23Background,
    onBackground = yim23Grey,
    surface = yim23DarkGray
)

@Composable
fun Yim23Theme(
    themeType : Number,
    systemUiController : SystemUiController = rememberSystemUiController(),
    content: @Composable () -> Unit
){
    val colorScheme =  when (themeType) {
        0 -> greenColorScheme
        1 -> redColorScheme
        2 -> blueColorScheme
        3 -> greyColorScheme
        else -> greenColorScheme
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = yim23Typography,
        content = content
    )

}