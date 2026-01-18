package org.listenbrainz.android.ui.screens.brainzplayer


import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.shared.model.Playable
import org.listenbrainz.shared.model.PlayableType
import org.listenbrainz.android.model.RepeatMode
import org.listenbrainz.shared.model.Song
import org.listenbrainz.shared.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.components.CustomSeekBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.PlayPauseIcon
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.SongViewPager
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.ListeningNowViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max

@ExperimentalMaterialApi
@Composable
fun BrainzPlayerBackDropScreen(
    modifier: Modifier = Modifier,
    backdropScaffoldState: BackdropScaffoldState,
    brainzPlayerViewModel: BrainzPlayerViewModel = viewModel(),
    listeningNowViewModel: ListeningNowViewModel = viewModel(),
    paddingValues: PaddingValues,
    isLandscape: Boolean = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
    backLayerContent: @Composable () -> Unit
) {
    val isShuffled by brainzPlayerViewModel.isShuffled.collectAsStateWithLifecycle()
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong
    val currentPlayableState =
        brainzPlayerViewModel.currentPlayable.collectAsStateWithLifecycle()
    var maxDelta by rememberSaveable {
        mutableFloatStateOf(0F)
    }
    val repeatMode by brainzPlayerViewModel.repeatMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val defaultBackgroundColor = ListenBrainzTheme.colorScheme.background
    val isDarkThemeEnabled = onScreenUiModeIsDark()
    val listeningNowUIState by listeningNowViewModel.listeningNowUIState.collectAsState()

    val isNothingPlaying = remember(currentlyPlayingSong) {
        currentlyPlayingSong.title == "null"
                && currentlyPlayingSong.artist == "null"
                || currentPlayableState.value.songs.isEmpty()
    }

    val isListeningNow = remember(listeningNowUIState) {
        listeningNowUIState.song != null && listeningNowUIState.song?.trackMetadata?.trackName?.isNotEmpty() == true
    }
    val scope = rememberCoroutineScope()

    /** 56.dp is default bottom navigation height */
    val headerHeight by animateDpAsState(
        targetValue = if (isLandscape) 0.dp else
            if (isNothingPlaying && !isListeningNow)
                56.dp
            else
                56.dp + ListenBrainzTheme.sizes.brainzPlayerPeekHeight
    )
    LaunchedEffect(currentlyPlayingSong, isDarkThemeEnabled) {
        brainzPlayerViewModel.updateBackgroundColorForPlayer(
            currentlyPlayingSong.albumArt,
            defaultBackgroundColor,
            context,
            isDarkThemeEnabled = isDarkThemeEnabled
        )
    }
    BackdropScaffold(
        modifier = modifier.padding(top = paddingValues.calculateTopPadding()),
        frontLayerShape = RectangleShape,
        backLayerBackgroundColor = Color.Transparent,
        frontLayerScrimColor = Color.Unspecified,
        headerHeight = headerHeight, // 126.dp is optimal header height.
        peekHeight = 0.dp,
        scaffoldState = backdropScaffoldState,
        backLayerContent = backLayerContent,
        frontLayerBackgroundColor = defaultBackgroundColor,
        appBar = {},
        persistentAppBar = false,
        frontLayerContent = {
            LaunchedEffect(Unit) {
                val delta = backdropScaffoldState.requireOffset()
                maxDelta = max(delta, maxDelta)
            }

            //To prevent screen showing null abruptly after listening now finishes
            if (!isListeningNow || !isNothingPlaying) {
                LaunchedEffect(isListeningNow) {
                    if (isNothingPlaying && backdropScaffoldState.isConcealed) {
                        backdropScaffoldState.reveal()
                    }
                }
                PlayerScreen(
                    currentlyPlayingSong = currentlyPlayingSong,
                    isShuffled = isShuffled,
                    repeatMode = repeatMode,
                    backdropScaffoldState = backdropScaffoldState,
                    backgroundBrush = Brush.verticalGradient(
                        colors = listOf(
                            brainzPlayerViewModel.playerBackGroundColor,
                            defaultBackgroundColor
                        )
                    ),
                    dynamicBackground = brainzPlayerViewModel.playerBackGroundColor,
                    currentPlayableState = currentPlayableState
                )
             
                if (!isLandscape) {
                    SongViewPager(
                        modifier = Modifier
                            .height(ListenBrainzTheme.sizes.brainzPlayerPeekHeight)
                            .graphicsLayer {
                                alpha =
                                    (backdropScaffoldState.requireOffset() / (maxDelta - headerHeight.toPx()))
                            },
                        songList = currentPlayableState.value.songs,
                        backdropScaffoldState = backdropScaffoldState,
                        currentlyPlayingSong = currentlyPlayingSong,
                        isLandscape = false
                    )
                }
            } else {
                LaunchedEffect(listeningNowUIState.song) {
                    if (listeningNowUIState.isListeningNow) {
                        listeningNowViewModel.updatePalette(context)
                    }
                }
                ListeningNowScreen(
                    viewModel = listeningNowViewModel,
                    backdropScaffoldState = backdropScaffoldState,
                    gradientBox = {
                        val backgroundColor =
                            listeningNowUIState.palette?.lightBacgroundColor
                                ?: ListenBrainzTheme.colorScheme.background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val value =
                                        (backdropScaffoldState.requireOffset() / (maxDelta - headerHeight.toPx()))
                                    alpha =
                                        if (value < 0.8f)
                                            (1f - value) * 0.25f else 0f
                                }
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            backgroundColor,
                                            Color(
                                                lerp(
                                                    backgroundColor.value.toLong(),
                                                    ListenBrainzTheme.colorScheme.background.value.toLong(),
                                                    0.5f
                                                )
                                            )
                                        )
                                    )
                                )
                        )
                    }
                )
                if (!isLandscape && backdropScaffoldState.isRevealed) {
                    ListeningNowCard(
                        uiState = listeningNowUIState,
                        isLandscape = false,
                        coroutineScope = scope,
                        backdropScaffoldState = backdropScaffoldState,
                        modifier = Modifier
                            .height(ListenBrainzTheme.sizes.brainzPlayerPeekHeight)
                            .graphicsLayer {
                                alpha =
                                    (backdropScaffoldState.requireOffset() / (maxDelta - headerHeight.toPx()))
                            }
                    )
                }

            }
        })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    brainzPlayerViewModel: BrainzPlayerViewModel = viewModel(),
    currentlyPlayingSong: Song,
    isShuffled: Boolean,
    repeatMode: RepeatMode,
    backdropScaffoldState: BackdropScaffoldState,
    backgroundBrush: Brush,
    dynamicBackground: Color = MaterialTheme.colorScheme.background,
    currentPlayableState: State<Playable>
) {
    val coroutineScope = rememberCoroutineScope()
    val playlistViewModel = koinViewModel<PlaylistViewModel>()
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val playlist = playlists.filter { it.id == (1).toLong() }
    val pagerState = rememberPagerState { currentPlayableState.value.songs.size }
    var listenLiked = false
    if (playlist.isNotEmpty()) {
        playlist[0].items.forEach {
            if (it.mediaID == currentlyPlayingSong.mediaID)
                listenLiked = true
        }
    }
    //For handling song change by list or buttons
    LaunchedEffect(currentPlayableState.value.currentSongIndex) {
        pagerState.scrollToPage(
            currentPlayableState.value.currentSongIndex
        )
    }
    //For handling song change by pager
    LaunchedEffect(pagerState.currentPage) {
        brainzPlayerViewModel.handleSongChangeFromPager(pagerState.currentPage)
    }
    if (backdropScaffoldState.isConcealed) {
        BackHandler {
            coroutineScope.launch {
                backdropScaffoldState.reveal()
            }
        }
    }

    val checkedSongs = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<Song>()
    }

    LazyColumn(
        modifier = Modifier
            .background(brush = backgroundBrush)
            .statusBarsPadding()
    ) {
        item {
            Spacer(Modifier.height(60.dp))
        }
        item {
            AlbumArtViewPager(currentlyPlayingSong, pagerState, dynamicBackground)
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(start = 25.dp, end = 25.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        text = currentlyPlayingSong.title,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .basicMarquee(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentlyPlayingSong.artist,
                        fontSize = 16.sp,
                        modifier = Modifier
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
                CustomSeekBar(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.98F)
                        .padding(horizontal = 20.dp),
                    progress = progress,
                    shape = CircleShape,
                    onValueChange = { newProgress ->
                        brainzPlayerViewModel.onSeek(newProgress)
                        brainzPlayerViewModel.onSeeked()
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(0.98F)
                    .padding(start = 22.dp, top = 10.dp, end = 22.dp)
            ) {
                val songCurrentPosition by brainzPlayerViewModel.songCurrentPosition.collectAsState()

                val (duration, currentPosition) = remember(
                    currentlyPlayingSong.duration,
                    songCurrentPosition
                ) {
                    val duration: String
                    val currentPosition: String
                    if (currentlyPlayingSong.duration / (1000 * 60 * 60) > 0 && songCurrentPosition / (1000 * 60 * 60) > 0) {
                        duration = String.format(
                            Locale.getDefault(),
                            "%02d:%02d:%02d",
                            currentlyPlayingSong.duration / (1000 * 60 * 60),
                            currentlyPlayingSong.duration / (1000 * 60) % 60,
                            currentlyPlayingSong.duration / 1000 % 60
                        )
                        currentPosition = String.format(
                            Locale.getDefault(),
                            "%02d:%02d:%02d",
                            songCurrentPosition / (1000 * 60 * 60),
                            songCurrentPosition / (1000 * 60) % 60,
                            songCurrentPosition / 1000 % 60
                        )
                    } else {
                        duration = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            currentlyPlayingSong.duration / (1000 * 60) % 60,
                            currentlyPlayingSong.duration / 1000 % 60
                        )
                        currentPosition = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            songCurrentPosition / (1000 * 60) % 60,
                            songCurrentPosition / 1000 % 60
                        )
                    }

                    return@remember duration to currentPosition
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
                    .padding(top = 20.dp, bottom = 20.dp)
            ) {
                Icon(
                    imageVector = when (repeatMode) {
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
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    (pagerState.currentPage - 1).coerceAtLeast(0)
                                )
                            }
                            brainzPlayerViewModel.skipToPreviousSong()
                        },
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
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                            brainzPlayerViewModel.skipToNextSong()
                        },
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Listening now",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = ListenBrainzTheme.paddings.defaultPadding),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val updatedSongs = currentPlayableState.value.songs.toMutableList().apply {
                            removeAll(checkedSongs)
                        }
                        brainzPlayerViewModel.changePlayable(
                            updatedSongs,
                            PlayableType.ALL_SONGS,
                            currentPlayableState.value.id,
                            updatedSongs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                .coerceAtLeast(0),
                            brainzPlayerViewModel.songCurrentPosition.value
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
                        .padding(end = ListenBrainzTheme.paddings.horizontal)
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
        itemsIndexed(
            items = currentPlayableState.value.songs
        ) { index, song ->
            val isChecked = checkedSongs.contains(song)
            BoxWithConstraints {
                val maxWidth = (this@BoxWithConstraints.maxWidth - 70.dp)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                )
                {
                    val modifier = if (currentlyPlayingSong.mediaID == song.mediaID) {
                        Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                    } else {
                        Modifier
                            .padding(
                                top = 6.dp,
                                bottom = 6.dp,
                                end = ListenBrainzTheme.paddings.smallPadding
                            )
                            .width(maxWidth)
                    }
                    ListenCardSmall(
                        modifier = modifier,
                        trackName = song.title,
                        artists = listOf(FeedListenArtist(song.artist, null, "")),
                        coverArtUrl = song.albumArt,
                        errorAlbumArt = R.drawable.ic_erroralbumart,
                        goToArtistPage = {}
                    ) {
                        brainzPlayerViewModel.skipToPlayable(index)
                        brainzPlayerViewModel.changePlayable(
                            currentPlayableState.value.songs,
                            PlayableType.ALL_SONGS,
                            currentPlayableState.value.id,
                            index,
                            0L
                        )
                        brainzPlayerViewModel.playOrToggleSong(song, true)
                    }
                    if (currentlyPlayingSong.mediaID != song.mediaID) {
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(5.dp),
                            shadowElevation = 5.dp,
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumArtViewPager(
    currentlyPlayingSong: Song,
    pagerState: PagerState,
    dynamicBackground: Color
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
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
                    .background(dynamicBackground)
                    .graphicsLayer {
                        // Calculate the absolute offset for the current page from the
                        // scroll position. We use the absolute value which allows us to mirror
                        // any effects for both directions
                        val pageOffset = pagerState.getOffsetDistanceInPages(
                            page.coerceIn(0, pagerState.pageCount)
                        ).absoluteValue

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
                Crossfade(
                    targetState = currentlyPlayingSong.albumArt,
                    modifier = Modifier.background(dynamicBackground)
                ) { albumArt ->
                    AsyncImage(
                        modifier = Modifier
                            .background(dynamicBackground)
                            .fillMaxSize()
                            .padding()
                            .clip(shape = RoundedCornerShape(20.dp))
                            .graphicsLayer { clip = true },
                        model = albumArt,
                        contentDescription = "",
                        error = painterResource(
                            id = R.drawable.ic_erroralbumart
                        ),
                        contentScale = ContentScale.FillBounds
                    )
                }

            }
        }
    }
    //  TODO("Fix View Pager changing pages")
}

@OptIn(ExperimentalFoundationApi::class)
@PreviewLightDark
@Composable
fun AlbumArtViewPagerPreview() {
    AlbumArtViewPager(
        currentlyPlayingSong = Song.preview(),
        pagerState = rememberPagerState { 3 },
        dynamicBackground = MaterialTheme.colorScheme.background
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun BrainzPlayerBackDropScreenPreview() {
    BrainzPlayerBackDropScreen(
        backdropScaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed),
        paddingValues = PaddingValues(0.dp)
    ) {}
}
