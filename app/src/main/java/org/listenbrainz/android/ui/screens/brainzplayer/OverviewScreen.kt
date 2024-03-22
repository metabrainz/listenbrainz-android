package org.listenbrainz.android.ui.screens.brainzplayer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerActivityCards
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun OverviewScreen (
    songsPlayedToday: List<Song>,
    goToRecentScreen: () -> Unit,
    goToArtistScreen: () -> Unit,
    goToAlbumScreen: () -> Unit,
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    artists : List<Artist>,
    albums: List<Album>
) {
    Column (modifier = Modifier.verticalScroll(rememberScrollState())) {
       RecentlyPlayedOverview(recentlyPlayedSongs = recentlyPlayedSongs, goToRecentScreen = goToRecentScreen ,brainzPlayerViewModel = brainzPlayerViewModel)
        ArtistsOverview(artists = artists, goToArtistScreen = goToArtistScreen)
        AlbumsOverview(albums = albums, goToAlbumScreen = goToAlbumScreen)
    }


}

@Composable
private fun RecentlyPlayedOverview(
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel : BrainzPlayerViewModel,
    goToRecentScreen : () -> Unit
) {
    Column(modifier = Modifier.background(
        brush = Brush.linearGradient(
            start = Offset.Zero,
            end = Offset(0f, Float.POSITIVE_INFINITY),
            colors = listOf(
                Color(0xFF111111),
                Color(0xFF131313),
                Color(0xFF151515),
                Color(0xFF171717),
                Color(0xFF272727),
                Color(0xFF272E27)
            )
        )
    ).padding(top = 15.dp, bottom = 15.dp)) {
        Text(
            "Recently Played", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ListenBrainzTheme.colorScheme.lbSignature
            ), modifier = Modifier.padding(start = 17.dp)
        )
        LazyRow(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(1010), Color(1010))
                    )
                )
                .height(250.dp)
        ) {
            items(items = recentlyPlayedSongs) { song ->
                if (song.title != "") {
                    BrainzPlayerActivityCards(icon = song.albumArt,
                        errorIcon = R.drawable.ic_artist,
                        title = song.title,
                        artist = song.artist,
                        modifier = Modifier
                            .clickable {
                                brainzPlayerViewModel.changePlayable(
                                    recentlyPlayedSongs,
                                    PlayableType.ALL_SONGS,
                                    song.mediaID,
                                    recentlyPlayedSongs.sortedBy { it.discNumber }.indexOf(song),
                                    0L
                                )
                                brainzPlayerViewModel.playOrToggleSong(song, true)
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                            .size(150.dp)
                            .clickable {
                                goToRecentScreen()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
                                .padding(start = 5.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                " All \n Recently\n Played",
                                style = TextStyle(fontSize = 20.sp),
                                color = ListenBrainzTheme.colorScheme.lbSignature
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistsOverview(
    artists : List<Artist>,
    goToArtistScreen : () -> Unit
) {
    Column(modifier = Modifier.background(
        brush = Brush.linearGradient(
            start = Offset.Zero,
            end = Offset(0f, Float.POSITIVE_INFINITY),
            colors = listOf(
                Color(0xFF111111),
                Color(0xFF131313),
                Color(0xFF151515),
                Color(0xFF171717),
                Color(0xFF272727),
                Color(0xFF272E27)
            )
        )
    ).padding(top = 15.dp, bottom = 15.dp)) {
        Text(
            "Artists", style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ListenBrainzTheme.colorScheme.lbSignature
            ), modifier = Modifier.padding(start = 17.dp)
        )
        LazyRow(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(1010), Color(1010))
                    )
                )
                .height(250.dp)
        ) {
            items(items = artists) { artist ->
                if (artist.name != "") {
                    BrainzPlayerActivityCards(
                        icon = "",
                        errorIcon = R.drawable.ic_artist,
                        title = "",
                        artist = artist.name,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                            .size(150.dp)
                            .clickable {
                                goToArtistScreen()
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))
                                .padding(start = 5.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                " All \n Artists",
                                style = TextStyle(fontSize = 20.sp),
                                color = ListenBrainzTheme.colorScheme.lbSignature
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun AlbumsOverview(
    albums: List<Album>,
    goToAlbumScreen: () -> Unit
){
   Column(modifier = Modifier.background(
        brush = Brush.linearGradient(
            start = Offset.Zero,
            end = Offset(0f, Float.POSITIVE_INFINITY),
            colors = listOf(
                Color(0xFF111111),
                Color(0xFF131313),
                Color(0xFF151515),
                Color(0xFF171717),
                Color(0xFF272727),
                Color(0xFF272E27)
            )
        )
    ).padding(top = 15.dp, bottom = 15.dp)){
        Text("Albums" , style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ListenBrainzTheme.colorScheme.lbSignature
        ) , modifier = Modifier.padding(start = 17.dp))
        LazyRow(
            modifier = Modifier
                .height(250.dp)){
            items(items = albums) {
                    album ->
                if(album.title != ""){
                    BrainzPlayerActivityCards(icon = album.albumArt,
                        errorIcon = R.drawable.ic_artist,
                        title = album.title,
                        artist = album.artist,
                    )
                }
                else{
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                            .size(150.dp)
                            .clickable {
                                goToAlbumScreen()
                            }
                    ){
                        Column (modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(start = 5.dp , bottom = 20.dp) , verticalArrangement = Arrangement.Bottom) {
                            Text(" All \n Albums" , style = TextStyle(fontSize = 20.sp) , color = ListenBrainzTheme.colorScheme.lbSignature)
                        }
                    }
                }

            }
        }
    }
}