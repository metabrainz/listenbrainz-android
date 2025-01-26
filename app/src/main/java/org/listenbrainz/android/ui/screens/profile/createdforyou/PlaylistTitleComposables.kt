package org.listenbrainz.android.ui.screens.profile.createdforyou

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.util.Utils.removeHtmlTags
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// PlaylistHeadingAndDescription composable is used to display the heading and description of a playlist.
@Composable
fun PlaylistHeadingAndDescription(
    title: String,
    tracksCount: Int,
    lastUpdatedDate: String,
    description: String,
    onShareClick: () -> Unit,
    onPlayAllClick: () -> Unit
) {
    var isReadMoreEnabled by remember {
        mutableStateOf(true)
    }
    var isHeadingExpanded by remember {
        mutableStateOf(false)
    }
    var isReadMoreRequired by remember {
        mutableStateOf<Boolean?>(null)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            maxLines = if (isHeadingExpanded) Int.MAX_VALUE else 1,
            color = ListenBrainzTheme.colorScheme.listenText,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                isHeadingExpanded = !isHeadingExpanded
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(
                text = "$tracksCount tracks",
                color = ListenBrainzTheme.colorScheme.onBackground
            )
            Text(
                text = " | Updated ${formatDateLegacy(lastUpdatedDate)}",
                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.clickable {
                if(isReadMoreRequired == true)
                isReadMoreEnabled = !isReadMoreEnabled
            }
        ) {
            Text(
                text = removeExcessiveSpaces(removeHtmlTags(description)).trim(),
                color = ListenBrainzTheme.colorScheme.listenText.copy(0.5f),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.1.sp,
                maxLines = if (isReadMoreEnabled) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { onTextLayout ->
                    isReadMoreEnabled = onTextLayout.hasVisualOverflow
                    if(isReadMoreRequired == null)
                    isReadMoreRequired = onTextLayout.hasVisualOverflow
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            if(isReadMoreRequired == true)
            Text(
                text = if (isReadMoreEnabled) "read more" else "read less",
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                ),
                color = ListenBrainzTheme.colorScheme.listenText
            )

        }
        Spacer(modifier = Modifier.height(16.dp))
        PlayAndShareButtons(
            modifier = Modifier.align(Alignment.End),
            onShareClick = onShareClick,
            onPlayAllClick = onPlayAllClick
        )
    }
}

// PlayAndShareButtons composable is used to display the Play All and Share buttons.
@Composable
fun PlayAndShareButtons(
    modifier: Modifier = Modifier,
    onPlayAllClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(modifier = modifier) {
        Button(
            onClick = onPlayAllClick,
            colors = ButtonColors(
                contentColor = Color.White,
                containerColor = lb_purple,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.Gray
            )
        ) {
            Image(
                painter = painterResource(R.drawable.playlist_play_btn),
                contentDescription = "Play All",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Play All")
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onShareClick,
            colors = IconButtonColors(
                contentColor = Color.White,
                containerColor = lb_purple,
                disabledContentColor = Color.Gray,
                disabledContainerColor = Color.Gray
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.playlist_share_btn),
                contentDescription = "Share",
                modifier = Modifier.size(20.dp)
            )
        }

    }
}

// formatDateLegacy function is used to format the date in MMM dd, h:mm a format.Eg: Jan 06, 12:14 AM
fun formatDateLegacy(inputDate: String): String {
    // Parse the input date string
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
    isoFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = isoFormat.parse(inputDate)

    // Format to the desired output pattern
    val outputFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.ENGLISH)
    outputFormat.timeZone = TimeZone.getDefault() // Adjust to local time zone
    return outputFormat.format(date!!)
}

fun removeExcessiveSpaces(input: String): String {
    return input.trim().replace(Regex("\\s+"), " ")
}


@Preview(showBackground = true)
@Composable
fun WeeklyExplorationCardPreview() {
    ListenBrainzTheme {
        PlaylistHeadingAndDescription(
            title = "Weekly Exploration",
            tracksCount = 10,
            lastUpdatedDate = "2025-01-06T00:14:18.151711+00:00",
            description = "This is a weekly exploration playlist. It contains tracks that you might like. This is a weekly exploration playlist. It contains tracks that you might like.",
            onShareClick = {},
            onPlayAllClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun WeeklyExplorationCardPreviewDark() {
    ListenBrainzTheme() {
        PlaylistHeadingAndDescription(
            title = "Weekly Exploration",
            tracksCount = 10,
            lastUpdatedDate = "2025-01-06T00:14:18.151711+00:00",
            description = "This is a weekly exploration playlist. It contains tracks that you might like. This is a weekly exploration playlist. It contains tracks that you might like.",
            onShareClick = {},
            onPlayAllClick = {}
        )
    }
}