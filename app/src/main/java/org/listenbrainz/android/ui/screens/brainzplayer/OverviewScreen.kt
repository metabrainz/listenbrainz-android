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
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    artists : List<Artist>,
    albums: List<Album>
) {
    Column (modifier = Modifier.verticalScroll(rememberScrollState())) {
       RecentlyPlayedOverview(recentlyPlayedSongs = recentlyPlayedSongs, goToRecentScreen = goToRecentScreen ,brainzPlayerViewModel = brainzPlayerViewModel)
        ArtistsOverview(artists = artists)
        AlbumsOverview(albums = albums)
    }


}

@Composable
private fun RecentlyPlayedOverview(
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel : BrainzPlayerViewModel,
    goToRecentScreen : () -> Unit
) {
    Text("Recently Played" , style = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = ListenBrainzTheme.colorScheme.lbSignature
    ) , modifier = Modifier.padding(start = 17.dp))
    LazyRow(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(1010), Color(1010))
                )
            )
            .height(250.dp)){
        items(items = recentlyPlayedSongs) {
                song ->
            if(song.title == ""){
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                        .size(150.dp)
                        .clickable {
                            goToRecentScreen()
                        }
                ){
                    Column (modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(start = 5.dp , bottom = 20.dp) , verticalArrangement = Arrangement.Bottom) {
                        Text(" All \n Recently\n Played" , style = TextStyle(fontSize = 20.sp) , color = ListenBrainzTheme.colorScheme.lbSignature)
                    }
                }
            }
            else{
                Log.v("pranav" , (song.lastListenedTo).toString())
                BrainzPlayerActivityCards(icon = song.albumArt,
                    errorIcon = R.drawable.ic_artist,
                    title = song.title,
                    artist = song.artist,
                    modifier = Modifier
                        .clickable {
                            brainzPlayerViewModel.changePlayable(recentlyPlayedSongs, PlayableType.ALL_SONGS, song.mediaID,recentlyPlayedSongs.sortedBy { it.discNumber }.indexOf(song),0L)
                            brainzPlayerViewModel.playOrToggleSong(song, true)
                        }
                )
            }
        }
    }
}

@Composable
private fun ArtistsOverview(
    artists : List<Artist>
) {
    Text("Artists" , style = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = ListenBrainzTheme.colorScheme.lbSignature
    ) , modifier = Modifier.padding(start = 17.dp))
    LazyRow(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(1010), Color(1010))
                )
            )
            .height(250.dp)){
        items(items = artists) {
                artist ->
            BrainzPlayerActivityCards(icon = "",
                errorIcon = R.drawable.ic_artist,
                title = "",
                artist = artist.name,
            )
        }
    }
}

@Composable
private fun AlbumsOverview(
    albums: List<Album>,
){
    Text("Albums" , style = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = ListenBrainzTheme.colorScheme.lbSignature
    ) , modifier = Modifier.padding(start = 17.dp))
    LazyRow(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(1010), Color(1010))
                )
            )
            .height(250.dp)){
        items(items = albums) {
                album ->
            BrainzPlayerActivityCards(icon = album.albumArt,
                errorIcon = R.drawable.ic_artist,
                title = album.title,
                artist = album.artist,
            )
        }
    }
}