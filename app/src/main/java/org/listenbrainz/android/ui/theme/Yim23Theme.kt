package org.listenbrainz.android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.listenbrainz.android.model.yimdata.Yim23ThemeData

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
    themeType : Yim23ThemeData,
    systemUiController : SystemUiController = rememberSystemUiController(),
    content: @Composable () -> Unit
){
    val colorScheme =  when (themeType) {
        Yim23ThemeData.GREEN -> greenColorScheme
        Yim23ThemeData.RED -> redColorScheme
        Yim23ThemeData.BLUE -> blueColorScheme
        Yim23ThemeData.GRAY -> greyColorScheme
        else -> greenColorScheme
    }

    val view = LocalView.current
    (view.context as Activity).window.statusBarColor = colorScheme.onBackground.toArgb()
    (view.context as Activity).window.navigationBarColor = colorScheme.onBackground.toArgb()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = yim23Typography(),
        content = content
    )

}