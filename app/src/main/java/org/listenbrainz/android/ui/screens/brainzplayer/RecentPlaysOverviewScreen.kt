package org.listenbrainz.android.ui.screens.brainzplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun RecentPlaysScreen(
    songsPlayedToday: List<Song>,
    songsPlayedThisWeek: List<Song>,
) {
    Column (modifier = Modifier
        .fillMaxSize()
        .padding(start = 17.dp, end = 17.dp)) {
        if(songsPlayedToday.isNotEmpty()){
            Text("Played Today" , color = ListenBrainzTheme.colorScheme.lbSignature , fontSize = 25.sp)
            Spacer(modifier = Modifier.height(10.dp))
            PlayedToday(songsPlayedToday = songsPlayedToday)
        }
        if(songsPlayedThisWeek.isNotEmpty()) {
            Text("Played This Week" , color = ListenBrainzTheme.colorScheme.lbSignature , fontSize = 25.sp)
            Spacer(modifier = Modifier.height(10.dp))
            PlayedThisWeek(songsPlayedThisWeek = songsPlayedThisWeek)
        }
    }
}
@Composable
private fun PlayedToday(
    songsPlayedToday: List<Song>
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedToday.size + 20.dp
    if(songsPlayedToday.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
       heightConstraint
    )) {
        items(songsPlayedToday){
            ListenCardSmall(trackName = it.title, artistName = it.artist, coverArtUrl = it.albumArt, enableDropdownIcon = true) {
                Unit
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun PlayedThisWeek(
    songsPlayedThisWeek: List<Song>
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedThisWeek.size + 20.dp
    if(songsPlayedThisWeek.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
        heightConstraint
    )) {
        items(songsPlayedThisWeek){
            ListenCardSmall(trackName = it.title, artistName = it.artist, coverArtUrl = it.albumArt, enableDropdownIcon = true) {
                Unit
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}