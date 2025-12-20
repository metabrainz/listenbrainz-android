package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.recordingSearch.RecordingData
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.formatDurationSeconds

@Composable
fun AddTrackToPlaylist(
    modifier: Modifier = Modifier,
    playlistDetailUIState: PlaylistDetailUIState,
    onTrackSelect: (RecordingData) -> Unit,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ListenBrainzTheme.colorScheme.listenText
                )
            }

            TextField(
                textStyle = TextStyle(
                    fontSize = 20.sp
                ),
                value = playlistDetailUIState.queryText ?: "",
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Enter track name or MBID",
                        style = TextStyle(fontSize = 20.sp),
                        color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.weight(1f)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = ListenBrainzTheme.colorScheme.listenText,
                    cursorColor = ListenBrainzTheme.colorScheme.listenText,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            IconButton(onClick = {
                onQueryChange("")
                focusRequester.requestFocus()
                keyboardController?.show()
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = ListenBrainzTheme.colorScheme.listenText
                )
            }
        }

        HorizontalDivider(color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.3f))

        // Search Results
        if (!playlistDetailUIState.isSearching) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Spacer(Modifier.height(12.dp))
                }
                items(playlistDetailUIState.queriedRecordings.size) { index ->
                    val recording = playlistDetailUIState.queriedRecordings[index]
                    ListenCardSmall(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        trackName = recording.title ?: "",
                        onClick = { onTrackSelect(recording) },
                        coverArtUrl = null,
                        artists = recording.artistCredit.map {
                            FeedListenArtist(
                                artistMbid = it.artist?.id,
                                artistCreditName = it.name ?: "",
                                joinPhrase = it.joinphrase
                            )
                        },
                        goToArtistPage = {},
                        trailingContent = {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp),
                                text = formatDurationSeconds(recording.length?.div(1000) ?: 0),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
                item {
                    Spacer(Modifier.height(48.dp))
                }
            }
        } else {
            Box(Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    color = ListenBrainzTheme.colorScheme.listenText,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 32.dp)
                )
            }
        }
    }
}


@Composable
fun AddTrackCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val themeColors = ListenBrainzTheme.colorScheme
    val sizes = ListenBrainzTheme.sizes

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(sizes.listenCardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(sizes.listenCardCorner),
        colors = CardDefaults.cardColors(containerColor = themeColors.level1),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // + Button
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(
                        themeColors.level2,
                        shape = RoundedCornerShape(sizes.listenCardCorner)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Track",
                    tint = themeColors.listenText
                )
            }

            // Text
            Text(
                text = "Add track to this playlist",
                color = themeColors.listenText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddToPlaylistPreview() {
    ListenBrainzTheme {
        AddTrackToPlaylist(
            playlistDetailUIState = PlaylistDetailUIState(),
            onTrackSelect = {},
            onQueryChange = {},
            onDismiss = {}
        )
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddTrackCardPreview() {
    ListenBrainzTheme {
        AddTrackCard(onClick = {})
    }
}