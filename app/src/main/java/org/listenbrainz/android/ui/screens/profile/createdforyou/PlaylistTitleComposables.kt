package org.listenbrainz.android.ui.screens.profile.createdforyou

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun WeeklyExplorationCard(
    title: String,
    tracksCount: Int,
    lastUpdatedDate: String,
    description: String
) {
    var isReadMoreEnabled by remember {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = ListenBrainzTheme.colorScheme.listenText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(
                text = "$tracksCount tracks",
                color = ListenBrainzTheme.colorScheme.onBackground
            )
            Text(
                text = " | Updated $lastUpdatedDate",
                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.clickable {
                isReadMoreEnabled = !isReadMoreEnabled
            }
        ) {
            Text(
                text = description,
                color = ListenBrainzTheme.colorScheme.listenText.copy(0.5f),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.1.sp,
                maxLines = if (isReadMoreEnabled) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { onTextLayout ->
                    isReadMoreEnabled = onTextLayout.hasVisualOverflow
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isReadMoreEnabled) "read more" else "read less",
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                ),
                color = ListenBrainzTheme.colorScheme.listenText
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun WeeklyExplorationCardPreview() {
    ListenBrainzTheme {
        WeeklyExplorationCard(
            title = "Weekly Exploration",
            tracksCount = 10,
            lastUpdatedDate = "2021-10-10",
            description = "This is a weekly exploration playlist. It contains tracks that you might like. This is a weekly exploration playlist. It contains tracks that you might like."
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun WeeklyExplorationCardPreviewDark() {
    ListenBrainzTheme() {
        WeeklyExplorationCard(
            title = "Weekly Exploration",
            tracksCount = 10,
            lastUpdatedDate = "2021-10-10",
            description = "This is a weekly exploration playlist. It contains tracks that you might like. This is a weekly exploration playlist. It contains tracks that you might like."
        )
    }
}