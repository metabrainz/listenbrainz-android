package org.listenbrainz.android.util


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.CustomSeekBar
import org.listenbrainz.android.ui.components.PlayPauseIcon
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun SongViewPager(
    modifier: Modifier = Modifier,
    backdropScaffoldState: BackdropScaffoldState,
    currentlyPlayingSong: Song,
    songList: List<Song>,
    viewModel: BrainzPlayerViewModel = koinViewModel(),
    isLandscape: Boolean
) {
    if (songList.isEmpty())
        return

    val coroutineScope = rememberCoroutineScope()
    val pagerState: PagerState = rememberPagerState(
        initialPage = songList
            .indexOfFirst { it.mediaID == currentlyPlayingSong.mediaID }
            .takeIf { it != -1 } ?: 0
    ) { songList.size }
    if (!isLandscape) {
        LaunchedEffect(pagerState.currentPage) {
            val newSong = songList[pagerState.currentPage]
            if (
                !newSong.isNothing()
                && newSong != currentlyPlayingSong
                && pagerState.currentPage != pagerState.settledPage
            ) {
                try {
                    viewModel.playOrToggleSong(newSong)
                } catch (e: Exception) {
                    Log.e(e)
                }
            }
        }

        LaunchedEffect(viewModel.appPreferences.currentPlayable?.currentSongIndex) {
            pagerState.scrollToPage(
                viewModel.appPreferences.currentPlayable?.currentSongIndex ?: 0
            )
        }
        LaunchedEffect(pagerState.currentPage) {
            viewModel.handleSongChangeFromPager(pagerState.currentPage)
        }
    }
    if (isLandscape) {
        PlayerContent(
            coroutineScope = coroutineScope,
            backdropScaffoldState = backdropScaffoldState,
            viewModel = viewModel,
            pagerState = pagerState,
            currentlyPlayingSong = currentlyPlayingSong,
            isLandscape = true
        )
    } else {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxWidth()
                .background(viewModel.playerBackGroundColor)
        ) {
            PlayerContent(
                coroutineScope = coroutineScope,
                backdropScaffoldState = backdropScaffoldState,
                viewModel = viewModel,
                pagerState = pagerState,
                currentlyPlayingSong = currentlyPlayingSong,
                isLandscape = false
            )
        }
    }
}

@Composable
fun PlayerContent(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    backdropScaffoldState: BackdropScaffoldState,
    viewModel: BrainzPlayerViewModel,
    isLandscape: Boolean,
    currentlyPlayingSong: Song,
    pagerState: PagerState
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                coroutineScope.launch {
                    // Click anywhere to open the front layer.
                    backdropScaffoldState.conceal()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val progress by viewModel.progress.collectAsState()
        CustomSeekBar(
            modifier = Modifier
                .height(10.dp)
                .fillMaxWidth(),
            progress = progress,
            onValueChange = { newProgress ->
                viewModel.onSeek(newProgress)
                viewModel.onSeeked()
            },
            remainingProgressColor = ListenBrainzTheme.colorScheme.hint
        )

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val playIcon by viewModel.playButton.collectAsState()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 4.dp)
                        .size(if (isLandscape) 30.dp else 45.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(shape = RoundedCornerShape(8.dp))
                            .graphicsLayer { clip = true },
                        model = currentlyPlayingSong.albumArt,
                        contentDescription = "",
                        error = painterResource(
                            id = R.drawable.ic_erroralbumart
                        ),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = if (!isLandscape) 35.dp else 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isLandscape)
                        PlayerControls(
                            playIcon = playIcon,
                            viewModel = viewModel,
                            pagerState = pagerState,
                            coroutineScope = coroutineScope
                        )
                    SongInfo(currentlyPlayingSong = currentlyPlayingSong)
                }
            }
            if (isLandscape) {
                PlayerControls(
                    coroutineScope = coroutineScope,
                    pagerState = null,
                    viewModel = viewModel,
                    playIcon = playIcon
                )
            }
        }
    }
}

@Composable
fun PlayerControls(
    playIcon: ImageVector,
    viewModel: BrainzPlayerViewModel,
    pagerState: PagerState?,
    coroutineScope: CoroutineScope
) {
    Row(
        modifier = Modifier
            .requiredHeightIn(min = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = "",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    if (pagerState != null) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage - 1).coerceAtLeast(0)
                            )
                        }
                    }
                    viewModel.skipToPreviousSong()
                },
            tint = MaterialTheme.colorScheme.onSurface
        )

        PlayPauseIcon(
            playIcon,
            viewModel,
            Modifier.size(35.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = "",
            Modifier
                .size(35.dp)
                .clickable { viewModel.skipToNextSong() },
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SongInfo(currentlyPlayingSong: Song) {
    Text(
        text = when {
            currentlyPlayingSong.artist == "null" && currentlyPlayingSong.title == "null" -> ""
            currentlyPlayingSong.artist == "null" -> currentlyPlayingSong.title
            currentlyPlayingSong.title == "null" -> currentlyPlayingSong.artist
            else -> currentlyPlayingSong.artist + "  -  " + currentlyPlayingSong.title
        },
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.basicMarquee()
    )
}

@Composable
fun Modifier.dynamicBackgroundFromAlbumArt(
    albumArtUrl: String?,
    defaultColor: Color = ListenBrainzTheme.colorScheme.level1,
    dullnessFactor: Float = 0.6f
) = composed {
    val context = LocalContext.current
    var backgroundColor by remember { mutableStateOf(defaultColor) }
    val animatedBackgroundColor by animateColorAsState(targetValue = backgroundColor)

    LaunchedEffect(albumArtUrl) {
        albumArtUrl?.let { url ->
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = result.image.toBitmap()
                val palette = Palette.from(bitmap).generate()
                val dominantColor = palette.getDominantColor(defaultColor.toArgb())
                val color = Color(dominantColor)

                backgroundColor = color.copy(
                    red = color.red * dullnessFactor + (1 - dullnessFactor) * 0.5f,
                    green = color.green * dullnessFactor + (1 - dullnessFactor) * 0.5f,
                    blue = color.blue * dullnessFactor + (1 - dullnessFactor) * 0.5f
                )
            } else {
                backgroundColor = defaultColor
            }
        }
    }

    Modifier.drawBehind {
        drawRect(color = animatedBackgroundColor, size = size)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SongViewPagerPreview() {
    SongViewPager(
        songList = listOf(),
        currentlyPlayingSong = Song.preview(),
        backdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed),
        isLandscape = false
    )
}
