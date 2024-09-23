package org.listenbrainz.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 6.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.inter_black)),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 12.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_light)),
        fontWeight = FontWeight.Normal,
        color = Color.Black,
        fontSize = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    
)

@Composable
fun getSize() : Pair<Int , Int> {
    val width = LocalConfiguration.current.screenHeightDp
    val height = LocalConfiguration.current.screenWidthDp

    return Pair(width , height)
}



@Composable
fun yim23Typography () : Typography {
    val width = getSize().first
    return Typography(
        bodySmall = TextStyle(
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily(Font(R.font.inter_black)),
            fontWeight = FontWeight(900),
            fontSize = 12.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily(Font(R.font.inter_black)),
            fontWeight = FontWeight(900),
            fontSize = 22.sp,
        ),
        labelLarge = TextStyle(
            fontSize = 70.sp,
            fontFamily = FontFamily(Font(R.font.inter_black)),
            fontWeight = FontWeight(900),
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily(Font(R.font.roboto_light)),
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily(Font(R.font.roboto_regular)),
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily(Font(R.font.inter_black)),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            lineHeight = 34.25.sp,
            letterSpacing = (0.010*width).sp,
        ),
    )
}