package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun SelectPlaylistBase(
    songName: String,
    modifier: Modifier = Modifier,
    onSelect: (UserPlaylist) -> Unit,
    onCreateNewPlaylist: () -> Unit,
    getUserPlaylist: (Int) -> UserPlaylist?,
    getCollabPlaylist: (Int) -> UserPlaylist?,
    userPlaylistDataSize: Int,
    collabPlaylistDataSize: Int,
    isRefreshing: Boolean,
    pullRefreshState: PullRefreshState,
    onCloseButtonClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(ListenBrainzTheme.colorScheme.background)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                IconButton(
                    onClick = onCloseButtonClick
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = null,
                        tint = ListenBrainzTheme.colorScheme.listenText
                    )
                }
                Text(
                    text = "Add to playlist",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ListenBrainzTheme.colorScheme.listenText
                )
            }


            Text(
                modifier = Modifier.padding(16.dp),
                text = buildAnnotatedString {
                    append("Add the track ")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)){
                        append(songName)
                    }
                    append(" to one or more of your playlists below:")
                },
                color = ListenBrainzTheme.colorScheme.text
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lb_purple.copy(alpha = 0.2f))
                            .clickable { onCreateNewPlaylist() }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = null,
                                tint = ListenBrainzTheme.colorScheme.text
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create new playlist", color = ListenBrainzTheme.colorScheme.text)
                        }
                    }
                }

                items(userPlaylistDataSize) { index ->
                    getUserPlaylist(index)?.let { playlist ->
                        PlaylistItem(playlist = playlist, onClick = { onSelect(playlist) })
                    }
                }

                if (collabPlaylistDataSize > 0) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Collaborative Playlists",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = ListenBrainzTheme.colorScheme.text,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(collabPlaylistDataSize) { index ->
                    getCollabPlaylist(index)?.let { playlist ->
                        PlaylistItem(playlist = playlist, onClick = { onSelect(playlist) })
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            state = pullRefreshState,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1
        )
    }
}


@Composable
fun PlaylistItem(playlist: UserPlaylist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = playlist.title?: "Title not available",
            style = MaterialTheme.typography.bodyLarge,
            color = ListenBrainzTheme.colorScheme.text
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = ListenBrainzTheme.colorScheme.dividerColor)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectPlaylistBasePreview() {
    val userPlaylists = List(3) { UserPlaylist("User Playlist ${it + 1}") }
    val collabPlaylists = List(2) { UserPlaylist("Collab Playlist ${it + 1}") }
    val isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {})

    ListenBrainzTheme {
        SelectPlaylistBase(
            songName = "Mannat",
            onSelect = {},
            onCreateNewPlaylist = {},
            getUserPlaylist = { index -> userPlaylists.getOrNull(index) },
            getCollabPlaylist = { index -> collabPlaylists.getOrNull(index) },
            userPlaylistDataSize = userPlaylists.size,
            collabPlaylistDataSize = collabPlaylists.size,
            isRefreshing = isRefreshing,
            pullRefreshState = pullRefreshState,
            onCloseButtonClick = {},
        )
    }
}
