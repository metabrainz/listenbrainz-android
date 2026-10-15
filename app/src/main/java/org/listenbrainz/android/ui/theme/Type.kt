package org.listenbrainz.android.ui.theme

import androidx.compose.material3.Typography
import org.listenbrainz.shared.resources.roboto_regular
import org.listenbrainz.shared.resources.roboto_light
import org.listenbrainz.shared.resources.inter_black
import org.listenbrainz.shared.resources.Res
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun listenBrainzTypography(): Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 6.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(Res.font.inter_black)),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 12.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(Res.font.roboto_light)),
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
