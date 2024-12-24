package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerDropDownMenu
import org.listenbrainz.android.ui.components.BrainzPlayerListenCard
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun RecentPlaysScreen(
    songsPlayedToday: List<Song>,
    songsPlayedThisWeek: List<Song>,
    onPlayIconClick: (Song) -> Unit,
    onAddToQueue : (Song) -> Unit,
    onPlayNext : (Song) -> Unit,
    onAddToNewPlaylist : (Song) -> Unit,
    onAddToExistingPlaylist : (Song) -> Unit
) {
    val dropdownState : MutableState<Pair<Int,Int>> = remember {mutableStateOf(Pair(-1,-1))}
    Column (modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxSize()) {
        if(songsPlayedToday.isNotEmpty()){
            Column(modifier = Modifier
                .background(
                    brush = ListenBrainzTheme.colorScheme.gradientBrush
                )
                .padding(top = 15.dp, bottom = 15.dp, start = 10.dp)) {
                Text(
                    "Played Today",
                    color = ListenBrainzTheme.colorScheme.lbSignature,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PlayedToday(songsPlayedToday = songsPlayedToday, onPlayIconClick = onPlayIconClick, dropDownState = dropdownState,  onAddToQueue = onAddToQueue, onAddToNewPlaylist = onAddToNewPlaylist, onAddToExistingPlaylist = onAddToExistingPlaylist, onPlayNext = onPlayNext)
            }
        }
        if(songsPlayedThisWeek.isNotEmpty()) {
            Column(modifier = Modifier
                .background(
                    brush = ListenBrainzTheme.colorScheme.gradientBrush
                )
                .padding(top = 15.dp, bottom = 15.dp, start = 10.dp)) {
                Text(
                    "Played This Week",
                    color = ListenBrainzTheme.colorScheme.lbSignature,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PlayedThisWeek(songsPlayedThisWeek = songsPlayedThisWeek, onPlayIconClick = onPlayIconClick, dropDownState = dropdownState, onAddToQueue = onAddToQueue, onAddToNewPlaylist = onAddToNewPlaylist, onAddToExistingPlaylist = onAddToExistingPlaylist, onPlayNext = onPlayNext)
            }
        }
    }
}
@Composable
private fun PlayedToday(
    songsPlayedToday: List<Song>,
    onPlayIconClick: (Song) -> Unit,
    dropDownState : MutableState<Pair<Int,Int>>,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToExistingPlaylist: (Song) -> Unit,
    onAddToNewPlaylist: (Song) -> Unit
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedToday.size + 20.dp
    if(songsPlayedToday.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
       heightConstraint
    )) {
        itemsIndexed(songsPlayedToday){
            index, it ->
            BrainzPlayerListenCard(title = it.title, subTitle = it.artist, coverArtUrl = it.albumArt, errorAlbumArt = R.drawable.ic_erroralbumart, onPlayIconClick = {onPlayIconClick(it)}, onDropdownIconClick = {dropDownState.value = Pair(1,index)}, dropDownState = dropDownState.value == Pair(1,index),dropDown = {BrainzPlayerDropDownMenu(
                expanded = dropDownState.value == Pair(1,index),
                onDismiss = {dropDownState.value = Pair(-1,-1)},
                onAddToQueue = {onAddToQueue(it)},
                onPlayNext =  {onPlayNext(it)},
                onAddToExistingPlaylist = {onAddToExistingPlaylist(it)},
                onAddToNewPlaylist = {onAddToNewPlaylist(it)}
            )})
            Spacer(modifier = Modifier.height(5.dp))
        }

    }
}

@Composable
private fun PlayedThisWeek(
    songsPlayedThisWeek: List<Song>,
    onPlayIconClick: (Song) -> Unit,
    dropDownState : MutableState<Pair<Int,Int>>,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToExistingPlaylist: (Song) -> Unit,
    onAddToNewPlaylist: (Song) -> Unit
){
    var heightConstraint = ListenBrainzTheme.sizes.listenCardHeight * songsPlayedThisWeek.size + 20.dp
    if(songsPlayedThisWeek.size > 4) heightConstraint = 250.dp
    LazyColumn (modifier = Modifier.height(
        heightConstraint
    )) {
        itemsIndexed(songsPlayedThisWeek){
            index, it ->
            BrainzPlayerListenCard(title = it.title, subTitle = it.artist, coverArtUrl = it.albumArt, errorAlbumArt = R.drawable.ic_erroralbumart, onPlayIconClick = {onPlayIconClick(it)}, onDropdownIconClick = {dropDownState.value = Pair(2,index)}, dropDownState = dropDownState.value == Pair(2,index),dropDown = {BrainzPlayerDropDownMenu(
                expanded = dropDownState.value == Pair(2,index),
                onDismiss = {dropDownState.value = Pair(-1,-1)},
                onAddToQueue = {onAddToQueue(it)},
                onPlayNext =  {onPlayNext(it)},
                onAddToExistingPlaylist = {onAddToExistingPlaylist(it)},
                onAddToNewPlaylist = {onAddToNewPlaylist(it)}
            )})
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}