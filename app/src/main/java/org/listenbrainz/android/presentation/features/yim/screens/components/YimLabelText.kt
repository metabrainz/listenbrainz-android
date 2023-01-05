package org.listenbrainz.android.presentation.features.yim.screens.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings

@Composable
fun YimLabelText(
    heading: String,
    subHeading: String
){
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_bold))
                )
            ){
                append(heading)
            }
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_light))
                )
            ){
                append("\n\n$subHeading")
            }
        },
        textAlign = TextAlign.Center,
        modifier = androidx.compose.ui.Modifier.padding(LocalYimPaddings.current.extraLargePadding)
    )
}