package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerActivityCards
import org.listenbrainz.android.ui.components.BrainzPlayerSeeAllCard
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun OverviewScreen(
    songsPlayedToday: List<Song>,
    goToRecentScreen: () -> Unit,
    goToArtistScreen: () -> Unit,
    goToAlbumScreen: () -> Unit,
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    artists: List<Artist>,
    albums: List<Album>,
    albumSongsMap: Map<Album, List<Song>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        RecentlyPlayedOverview(
            modifier = Modifier.fillMaxWidth(),
            recentlyPlayedSongs = recentlyPlayedSongs,
            goToRecentScreen = goToRecentScreen,
            onClick = { song ->
                brainzPlayerViewModel.changePlayable(
                    recentlyPlayedSongs,
                    PlayableType.ALL_SONGS,
                    song.mediaID,
                    recentlyPlayedSongs.indexOf(song),
                    0L
                )
                brainzPlayerViewModel.playOrToggleSong(song, true)
            }
        )
        ArtistsOverview(
            modifier = Modifier.fillMaxWidth(),
            artists = artists,
            goToArtistScreen = goToArtistScreen,
            onClick = { artist ->
                brainzPlayerViewModel.changePlayable(
                    artist.songs,
                    PlayableType.ARTIST,
                    artist.id,
                    0,
                    0L
                )
                artist.songs.getOrNull(0)?.let {
                    brainzPlayerViewModel.playOrToggleSong(it, true)
                }
            }
        )
        AlbumsOverview(
            modifier = Modifier.fillMaxWidth(),
            albums = albums,
            goToAlbumScreen = goToAlbumScreen,
            onClick = { album ->
                val albumSongs = albumSongsMap[album]?.sortedBy { it.trackNumber }
                if (!albumSongs.isNullOrEmpty()) {
                    brainzPlayerViewModel.changePlayable(
                        albumSongs,
                        PlayableType.ALBUM,
                        album.albumId,
                        0,
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(albumSongs[0], true)
                }
            }
        )
    }
}

@Composable
private fun RecentlyPlayedOverview(
    modifier: Modifier = Modifier,
    recentlyPlayedSongs: List<Song>,
    goToRecentScreen: () -> Unit,
    onClick: (Song) -> Unit
) {
    Column(
        modifier = modifier
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(top = 15.dp, bottom = 15.dp)
    ) {
        Text(
            "Recently Played", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ListenBrainzTheme.colorScheme.lbSignature
            ), modifier = Modifier.padding(start = 17.dp)
        )
        LazyRow(
            modifier = Modifier
                .height(250.dp)
        ) {
            items(items = recentlyPlayedSongs) { song ->
                BrainzPlayerActivityCards(
                    icon = song.albumArt,
                    errorIcon = R.drawable.ic_song,
                    title = song.artist,
                    subtitle = song.title,
                    modifier = Modifier.clickable {
                        onClick(song)
                    }
                )
            }
            if (recentlyPlayedSongs.isNotEmpty())
                item {
                    BrainzPlayerSeeAllCard(
                        onCardClicked = goToRecentScreen,
                        cardText = " All \n Recently\n Played"
                    )
                }
        }
    }
}

@Composable
private fun ArtistsOverview(
    modifier: Modifier = Modifier,
    artists: List<Artist>,
    goToArtistScreen: () -> Unit,
    onClick: (Artist) -> Unit
) {
    Column(
        modifier = modifier
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(top = 15.dp, bottom = 15.dp)
    ) {
        Text(
            "Artists", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ListenBrainzTheme.colorScheme.lbSignature
            ), modifier = Modifier.padding(start = 17.dp)
        )
        LazyRow(
            modifier = Modifier
                .height(250.dp)
        ) {
            items(items = artists) { artist ->
                BrainzPlayerActivityCards(
                    modifier = Modifier.clickable {
                        onClick(artist)
                    },
                    icon = "",
                    errorIcon = R.drawable.ic_artist,
                    title = "",
                    subtitle = artist.name,
                )
            }
            if (artists.isNotEmpty()) {
                item {
                    BrainzPlayerSeeAllCard(
                        onCardClicked = goToArtistScreen,
                        cardText = " All \n Artists"
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsOverview(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    goToAlbumScreen: () -> Unit,
    onClick: (Album) -> Unit
) {
    Column(
        modifier = modifier
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(top = 15.dp, bottom = 15.dp)
    ) {
        Text(
            "Albums", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ListenBrainzTheme.colorScheme.lbSignature
            ), modifier = Modifier.padding(start = 17.dp)
        )
        LazyRow(
            modifier = Modifier
                .height(250.dp)
        ) {
            items(items = albums) { album ->
                BrainzPlayerActivityCards(
                    modifier = Modifier.clickable {
                        onClick(album)
                    },
                    icon = album.albumArt,
                    errorIcon = R.drawable.ic_album,
                    title = album.artist,
                    subtitle = album.title,
                )
            }
            item {
                BrainzPlayerSeeAllCard(
                    onCardClicked = goToAlbumScreen,
                    cardText = " All \n Albums"
                )
            }
        }
    }
}