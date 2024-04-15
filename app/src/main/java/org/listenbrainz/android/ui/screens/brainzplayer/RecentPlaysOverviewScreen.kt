package org.listenbrainz.android.ui.screens.brainzplayer

import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerListenCard
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun RecentPlaysScreen(
    songsPlayedToday: List<Song>,
    songsPlayedThisWeek: List<Song>,
    onPlayIconClick: (Song, List<Song>) -> Unit
) {
    Column (modifier = Modifier
        .fillMaxSize()
        .padding(start = 17.dp, end = 17.dp)) {
        if(songsPlayedToday.isNotEmpty()){
            Column(modifier = Modifier
                .background(
                    brush = ListenBrainzTheme.colorScheme.gradientBrush
                )
                .padding(top = 15.dp, bottom = 15.dp)) {
                Text(
                    "Played Today",
                    color = ListenBrainzTheme.colorScheme.lbSignature,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PlayedToday(songsPlayedToday = songsPlayedToday, onPlayIconClick = onPlayIconClick)
            }
        }
        if(songsPlayedThisWeek.isNotEmpty()) {
            Column(modifier = Modifier
                .background(
                    brush = ListenBrainzTheme.colorScheme.gradientBrush
                )
                .padding(top = 15.dp, bottom = 15.dp)) {
                Text(
                    "Played This Week",
                    color = ListenBrainzTheme.colorScheme.lbSignature,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PlayedThisWeek(songsPlayedThisWeek = songsPlayedThisWeek, onPlayIconClick = onPlayIconClick)
            }
        }
    }
}
@Composable
private fun PlayedToday(
    songsPlayedToday: List<Song>,
    onPlayIconClick: (Song, List<Song>) -> Unit
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedToday.size + 20.dp
    if(songsPlayedToday.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
       heightConstraint
    )) {
        items(songsPlayedToday){
            BrainzPlayerListenCard(title = it.title, subTitle = it.artist, coverArtUrl = it.albumArt, onPlayIconClick = {onPlayIconClick(it,songsPlayedToday)})
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun PlayedThisWeek(
    songsPlayedThisWeek: List<Song>,
    onPlayIconClick: (Song, List<Song>) -> Unit
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedThisWeek.size + 20.dp
    if(songsPlayedThisWeek.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
        heightConstraint
    )) {
        items(songsPlayedThisWeek){
            BrainzPlayerListenCard(title = it.title, subTitle = it.artist, coverArtUrl = it.albumArt, onPlayIconClick = {onPlayIconClick(it,songsPlayedThisWeek)})
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}