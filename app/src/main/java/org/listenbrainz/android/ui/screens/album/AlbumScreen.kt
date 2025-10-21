package org.listenbrainz.android.ui.screens.album

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.artist.BioCard
import org.listenbrainz.android.ui.screens.artist.Links
import org.listenbrainz.android.ui.screens.artist.ReviewsCard
import org.listenbrainz.android.ui.screens.artist.formatNumber
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.screens.profile.stats.ArtistCard
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.LinkUtils.parseLinks
import org.listenbrainz.android.viewmodel.AlbumViewModel
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun AlbumScreen(
    albumMbid: String,
    viewModel: AlbumViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    snackBarState: SnackbarHostState,
    topBarActions: TopBarActions
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAlbumData(albumMbid)
    }
    val uiState by viewModel.uiState.collectAsState()
    AlbumScreen(
        uiState = uiState,
        socialViewModel = socialViewModel,
        snackBarState = snackBarState,
        albumMbid = albumMbid,
        topBarActions = topBarActions
    )
}

@Composable
private fun AlbumScreen(
    uiState: AlbumUiState,
    socialViewModel: SocialViewModel,
    snackBarState: SnackbarHostState,
    albumMbid: String,
    topBarActions: TopBarActions
) {
    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Album.title
        )
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { isLoading ->
            if (isLoading) {
                LoadingAnimation()
            } else {
                LazyColumn {
                    item {
                        BioCard(
                            header = uiState.name,
                            coverArt = uiState.coverArt,
                            displayRadioButton = false,
                            useWebView = false,
                            totalPlays = uiState.totalPlays,
                            totalListeners = uiState.totalListeners,
                            artists = uiState.artists,
                            albumType = uiState.type,
                            albumReleaseDate = uiState.releaseDate,
                            albumTags = uiState.tags
                        )
                    }
                    item {
                        ArtistRadio()
                    }
                    item {
                        val artistMbid = when (uiState.artists.isNotEmpty()) {
                            true -> uiState.artists[0]?.artistMbid
                            false -> null
                        }
                        val links = when (uiState.artists.isNotEmpty()) {
                            true -> uiState.artists[0]?.rels
                            false -> null
                        }
                        Links(
                            parseLinks(artistMbid, links)
                        )
                    }
                    item {
                        TrackListCard(uiState = uiState)
                    }
                    item {
                        TopListenersCard(uiState = uiState)
                    }
                    item {
                        if (uiState.name != null) {
                            ReviewsCard(
                                reviewOfEntity = uiState.reviews,
                                socialViewModel = socialViewModel,
                                snackBarState = snackBarState,
                                goToUserPage = {},
                                onErrorShown = { socialViewModel.clearErrorFlow() },
                                onMessageShown = { socialViewModel.clearMsgFlow() },
                                albumMbid = albumMbid,
                                albumName = uiState.name
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistRadio() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    start = Offset.Zero,
                    end = Offset(0f, Float.POSITIVE_INFINITY),
                    colors = listOf(
                        Color(0xFF1E1E24),
                        Color(0xFF1F1E25),
                        Color(0xFF201F28),
                        Color(0xFF1F1F27),
                        Color(0xFF201F29),
                        Color(0xFF21202C),
                        Color(0xFF232233),
                        Color(0xFF242235)
                    )
                )
            )
            .padding(start = 23.dp, top = 18.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.lb_radio_play_button),
                contentDescription = null,
                tint = new_app_bg_light,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                "Artist Radio",
                color = new_app_bg_light,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp)
            )
            Spacer(modifier = Modifier.fillMaxWidth(0.8f))
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = new_app_bg_light,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
        }
    }
}

@Composable
private fun TrackListCard(
    uiState: AlbumUiState
) {
    val trackListCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val trackList = when (trackListCollapsibleState.value) {
        true -> uiState.trackList.take(5)
        false -> uiState.trackList
    }
    Box(
        modifier = Modifier
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .fillMaxWidth()
            .padding(23.dp)
    ) {
        Column {
            Text(
                "Tracklist",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            trackList.map {
                ListenCardSmall(
                    trackName = it?.name ?: "",
                    artists = it?.artists ?: listOf(),
                    coverArtUrl = uiState.coverArt,
                    goToArtistPage = {}) {}
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (uiState.trackList.size > 5) {
                    LoadMoreButton(state = trackListCollapsibleState.value) {
                        trackListCollapsibleState.value = !trackListCollapsibleState.value
                    }
                }
            }
        }
    }
}

@Composable
private fun TopListenersCard(
    uiState: AlbumUiState
) {
    val topListenersCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val topListeners = when (topListenersCollapsibleState.value) {
        true -> uiState.topListeners.take(5)
        false -> uiState.topListeners
    }
    Box(
        modifier = Modifier
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .fillMaxWidth()
            .padding(23.dp)
    ) {
        Column {
            Text(
                "Top listeners",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            topListeners.map {
                ArtistCard(
                    artistName = it?.userName ?: "",
                    listenCountLabel = formatNumber(it?.listenCount ?: 0)
                ) {

                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                if (uiState.topListeners.size > 5) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = topListenersCollapsibleState.value
                    ) {
                        topListenersCollapsibleState.value = !topListenersCollapsibleState.value
                    }
                }
            }
        }
    }
}
