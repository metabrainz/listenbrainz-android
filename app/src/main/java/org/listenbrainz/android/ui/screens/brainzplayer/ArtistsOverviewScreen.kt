package org.listenbrainz.android.ui.screens.brainzplayer

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.ArtistViewModel

@Composable
fun ArtistsScreenOverview(
    artists: List<Artist>
) {
    val artistsStarting: MutableMap<Char, MutableList<Artist>> = mutableMapOf()
    for (i in 0..25) {
        artistsStarting['A' + i] = mutableListOf()
    }

    for (i in 1..artists.size) {
        artistsStarting[artists[i - 1].name[0]]?.add(artists[i - 1])
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        for (i in 0..25) {
            val startingLetter: Char = ('A' + i)
            if (artistsStarting[startingLetter]!!.size > 0) {
                Text(
                    startingLetter.toString(), modifier = Modifier.padding(start = 10.dp , top = 15.dp , bottom = 15.dp) , style = TextStyle(
                        color = ListenBrainzTheme.colorScheme.lbSignature,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_bold)),
                    )
                )
                for (j in 1..artistsStarting[startingLetter]!!.size) {
                    var coverArt: String? = null
                    if (artistsStarting[startingLetter]!![j - 1].albums.isNotEmpty())
                        coverArt = artistsStarting[startingLetter]!![j - 1].albums[0].albumArt
                    ListenCardSmall(
                        trackName = artistsStarting[startingLetter]!![j - 1].name,
                        artistName = "${artistsStarting[startingLetter]!![j - 1].songs.size} tracks",
                        coverArtUrl = coverArt,
                        modifier = Modifier.padding(10.dp)
                    ) {

                    }
                }
            }
        }
    }
}