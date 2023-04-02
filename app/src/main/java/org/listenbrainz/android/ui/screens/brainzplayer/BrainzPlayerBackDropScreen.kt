package org.listenbrainz.android.ui.screens.brainzplayer

import CacheService
import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Playlist.Companion.recentlyPlayed
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.service.BrainzPlayerService
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.PlayPauseIcon
import org.listenbrainz.android.ui.components.SeekBar
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.util.BrainzPlayerExtensions.duration
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.Constants.RECENTLY_PLAYED_KEY
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.SongViewPager
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel
import kotlin.math.absoluteValue
import kotlin.math.max


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BrainzPlayerBackDropScreen(
    backdropScaffoldState: BackdropScaffoldState,
    brainzPlayerViewModel: BrainzPlayerViewModel = viewModel(),
    backLayerContent: @Composable () -> Unit
) {
    val isShuffled by brainzPlayerViewModel.isShuffled.collectAsState()
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    var maxDelta by rememberSaveable {
        mutableStateOf(0F)
    }
    val repeatMode by brainzPlayerViewModel.repeatMode.collectAsState()
    val headerHeight by animateDpAsState(targetValue = if (currentlyPlayingSong.title == "null" && currentlyPlayingSong.artist == "null") 0.dp else 136.dp)

    BackdropScaffold(
        frontLayerShape = RectangleShape,
        backLayerBackgroundColor = MaterialTheme.colorScheme.background,
        frontLayerScrimColor = Color.Unspecified,
        headerHeight = headerHeight, // 136.dp is optimal header height.
        peekHeight = 0.dp,
        scaffoldState = backdropScaffoldState,
        backLayerContent = {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                backLayerContent()
            }
        },
        frontLayerBackgroundColor = MaterialTheme.colorScheme.background,
        appBar = {},
        persistentAppBar = false,
        frontLayerContent = {
            val delta = backdropScaffoldState.offset.value
            maxDelta = max(delta, maxDelta)
            PlayerScreen(
                currentlyPlayingSong = currentlyPlayingSong,
                isShuffled = isShuffled,
                repeatMode = repeatMode
            )
            SongViewPager(
                modifier = Modifier.graphicsLayer {
                    alpha = ( delta / (maxDelta - headerHeight.toPx()) )
                },
                backdropScaffoldState = backdropScaffoldState
            )
        })
}


@ExperimentalPagerApi
@Composable
fun AlbumArtViewPager(viewModel: BrainzPlayerViewModel) {
    val songList = viewModel.mediaItem.collectAsState().value
    val currentlyPlayingSong = viewModel.currentlyPlayingSong.collectAsState().value.toSong
    val pagerState = viewModel.pagerState.collectAsState().value
    val pageState = rememberPagerState(initialPage = pagerState)
    songList.data?.let {
        HorizontalPager(
            count = it.size, state = pageState,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background
                ),
        ) { page ->
            Column(
                Modifier
                    .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .height(280.dp)
                        .padding(top = 20.dp)
                        .width(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .graphicsLayer {
                            // Calculate the absolute offset for the current page from the
                            // scroll position. We use the absolute value which allows us to mirror
                            // any effects for both directions
                            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                            // We animate the scaleX + scaleY, between 85% and 100%
                            lerp(
                                start = 0.85f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            ).also { scale ->
                                scaleX = scale
                                scaleY = scale
                            }

                            // We animate the alpha, between 50% and 100%
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .padding()
                            .clip(shape = RoundedCornerShape(20.dp))
                            .graphicsLayer { clip = true },
                        model = currentlyPlayingSong.albumArt,
                        contentDescription = "",
                        error = painterResource(
                            id = R.drawable.ic_erroralbumart
                        ),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
        //  TODO("Fix View Pager changing pages")
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlayerScreen(
    brainzPlayerViewModel: BrainzPlayerViewModel = viewModel(),
    currentlyPlayingSong: Song,
    isShuffled: Boolean,
    repeatMode: RepeatMode
) {
    val coroutineScope = rememberCoroutineScope()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val playlist = playlists.filter { it.id == (1).toLong() }
    var listenLiked = false
    if (playlist.isNotEmpty()) {
        playlist[0].items.forEach {
            if (it.mediaID == currentlyPlayingSong.mediaID)
                listenLiked = true
        }
    } else {
        println("Playlist is empty")
    }
    LazyColumn {
        item {
            AlbumArtViewPager(viewModel = brainzPlayerViewModel)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        text = currentlyPlayingSong.title,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(start = 25.dp)
                            .basicMarquee(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentlyPlayingSong.artist,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(start = 25.dp)
                            .basicMarquee(),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Icon(
                    painterResource(id = if (!listenLiked) R.drawable.ic_not_liked else R.drawable.ic_liked),
                    contentDescription = null,
                    Modifier
                        .padding(5.dp)
                        .clickable {
                            var li = playlists.filter { it.id == (1).toLong() }
                            coroutineScope.launch {
                                if (!li[0].items.contains(currentlyPlayingSong)) {
                                    playlistViewModel.addSongToPlaylist(
                                        currentlyPlayingSong,
                                        li[0]
                                    )
                                } else {
                                    playlistViewModel.deleteSongFromPlaylist(
                                        currentlyPlayingSong,
                                        li[0]
                                    )
                                }
                            }
                        },

                    tint = if (listenLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item {
            Box {
                val progress by brainzPlayerViewModel.progress.collectAsState()
                SeekBar(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.98F)
                        .padding(10.dp),
                    progress = progress,
                    onValueChange = brainzPlayerViewModel::onSeek,
                    onValueChanged = brainzPlayerViewModel::onSeeked
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.98F)
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp)) {
                val song by brainzPlayerViewModel.currentlyPlayingSong.collectAsState()
                val songCurrentPosition by brainzPlayerViewModel.songCurrentPosition.collectAsState()
                var duration = "00:00"
                var currentPosition = "00:00"
                if (song.duration / (1000 * 60 * 60) > 0 &&  songCurrentPosition / (1000 * 60 * 60) > 0){
                    duration =String.format("%02d:%02d:%02d", song.duration/(1000 * 60 * 60),song.duration/(1000 * 60) % 60,song.duration/1000 % 60)
                    currentPosition = String.format("%02d:%02d:%02d", songCurrentPosition/(1000 * 60 * 60),songCurrentPosition/(1000 * 60) % 60,songCurrentPosition/1000 % 60)
                }else{
                    duration = String.format("%02d:%02d",song.duration/(1000 * 60) % 60,song.duration/1000 % 60)
                    currentPosition = String.format("%02d:%02d",songCurrentPosition/(1000 * 60) % 60,songCurrentPosition/1000 % 60)
                }


                Text(
                    text = currentPosition,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 5.dp)
                )

                Text(
                    text = duration,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
        item {
            val playIcon by brainzPlayerViewModel.playButton.collectAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
            ) {
                Icon(
                    imageVector =    when(repeatMode) {
                        RepeatMode.REPEAT_MODE_OFF -> Icons.Rounded.Loop
                        RepeatMode.REPEAT_MODE_ALL -> Icons.Filled.RepeatOn
                        RepeatMode.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                    },
                    contentDescription = "",
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                        .clickable {
                            brainzPlayerViewModel.repeatMode()
                        },
                    tint = MaterialTheme.colorScheme.surfaceTint
                )

                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "",
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                        .clickable { brainzPlayerViewModel.skipToPreviousSong() },
                    tint = MaterialTheme.colorScheme.surfaceTint
                )

                LargeFloatingActionButton(onClick = {
                    brainzPlayerViewModel.playOrToggleSong(
                        brainzPlayerViewModel.currentlyPlayingSong.value.toSong,
                        brainzPlayerViewModel.isPlaying.value,
                    )
                }
                ) {
                    PlayPauseIcon(
                        icon = playIcon,
                        viewModel = brainzPlayerViewModel,
                        modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                        tint = MaterialTheme.colorScheme.surfaceTint

                    )
                }
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "",
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                        .clickable { brainzPlayerViewModel.skipToNextSong() },
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
                Icon(
                    imageVector = if (isShuffled) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                    contentDescription = "",
                    modifier = Modifier
                        .size(FloatingActionButtonDefaults.LargeIconSize)
                        .clickable { brainzPlayerViewModel.shuffle() },
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }
        val checkedSongs = mutableStateListOf<Song>()
        val songs = BrainzPlayerService.playableSongs ?: mutableListOf()
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Now Playing",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 25.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        checkedSongs.forEach { song ->
                            BrainzPlayerService.playableSongs.remove(song)
                        }
                        brainzPlayerViewModel.changePlayable(
                            BrainzPlayerService.playableSongs,
                            PlayableType.ALL_SONGS,
                            LBSharedPreferences.currentPlayable?.id ?: 0,
                            BrainzPlayerService.playableSongs.indexOfFirst { it.mediaID == currentlyPlayingSong.mediaID } ?: 0,brainzPlayerViewModel.songCurrentPosition.value
                        )
                        brainzPlayerViewModel.queueChanged(
                            currentlyPlayingSong,
                            brainzPlayerViewModel.isPlaying.value
                        )
                        checkedSongs.clear()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = checkedSongs.isNotEmpty(),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                        .alpha(if (checkedSongs.isNotEmpty()) 1f else 0f)
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "",
                    )
                }
            }
        }
        // Playlist
        itemsIndexed(items = BrainzPlayerService.playableSongs) {index, song ->
            val isChecked = checkedSongs.contains(song)
            BoxWithConstraints {
                val maxWidth =
                    (maxWidth - 60.dp).coerceAtMost(600.dp)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    val modifier = if (currentlyPlayingSong.mediaID == song.mediaID) {
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp).fillMaxWidth()
                    } else {
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp).width(maxWidth)
                    }
                    ListenCardSmall(
                        modifier = modifier,
                        releaseName = song.title,
                        artistName = song.artist,
                        coverArtUrl = song.albumArt,
                        imageLoadSize = 200,
                        useSystemTheme = true,
                        errorAlbumArt = R.drawable.ic_erroralbumart
                    ) {
                        brainzPlayerViewModel.skipToPlayable(index)
                        brainzPlayerViewModel.changePlayable(BrainzPlayerService.playableSongs, PlayableType.ALL_SONGS, LBSharedPreferences.currentPlayable?.id ?: 0, index,0L)
                        brainzPlayerViewModel.playOrToggleSong(song, true)
                    }
                    if (currentlyPlayingSong.mediaID!=song.mediaID) {
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(5.dp),
                            shadowElevation = 5.dp
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (isChecked) {
                                        checkedSongs.remove(song)
                                    } else {
                                        checkedSongs.add(song)
                                    }
                                },
                                modifier = Modifier
                                    .size(45.dp),
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = MaterialTheme.colorScheme.onSurface,
                                    disabledColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface,
                                )
                            )
                        }
                    }
                }
            }
        }
        item {
            // Fixes bottom nav bar overlapping over last song
            Spacer(modifier = Modifier.height(56.dp))
        }
    }

    // TODO: fix this
    val cache= App.context?.let { CacheService<Song>(it, RECENTLY_PLAYED_KEY) }
    cache?.saveData(currentlyPlayingSong, Song::class.java)
    val data= cache?.getData(Song::class.java)
    if (data != null) {
        recentlyPlayed.items=data.filter { it.title!="null" }.toList().reversed()
    }
}

