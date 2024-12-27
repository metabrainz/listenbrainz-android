package org.listenbrainz.android.util

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.CustomSeekBar
import org.listenbrainz.android.ui.components.PlayPauseIcon
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SongViewPager(
    modifier: Modifier = Modifier,
    backdropScaffoldState: BackdropScaffoldState,
    currentlyPlayingSong: Song,
    songList: List<Song>,
    viewModel: BrainzPlayerViewModel = hiltViewModel()
) {
    if (songList.isEmpty())
        return

    val coroutineScope = rememberCoroutineScope()
    val pagerState: PagerState = rememberPagerState(
        initialPage = songList
            .indexOfFirst { it.mediaID == currentlyPlayingSong.mediaID }
            .takeIf { it != -1 } ?: 0
    ) { songList.size }

    LaunchedEffect(pagerState.settledPage) {
        val newSong = songList[pagerState.settledPage]
        if (currentlyPlayingSong.mediaID != 0L && newSong != currentlyPlayingSong) {
            viewModel.playOrToggleSong(newSong)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .dynamicBackgroundFromAlbumArt(songList[pagerState.currentPage].albumArt)
    ) { index ->
        val song = songList[index]

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    coroutineScope.launch {
                        // Click anywhere to open the front layer.
                        backdropScaffoldState.conceal()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                val progress by viewModel.progress.collectAsState()
                CustomSeekBar(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(),
                    progress = progress,
                    onValueChange = { newProgress ->
                        viewModel.onSeek(newProgress)
                        viewModel.onSeeked()
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .height(45.dp)
                            .width(45.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(shape = RoundedCornerShape(8.dp))
                                .graphicsLayer { clip = true },
                            model = song.albumArt,
                            contentDescription = "",
                            error = painterResource(
                                id = R.drawable.ic_erroralbumart
                            ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    val playIcon by viewModel.playButton.collectAsState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 35.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "",
                                Modifier
                                    .size(35.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(
                                                (pagerState.currentPage - 1).coerceAtLeast(0)
                                            )
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
                        Text(
                            text = when {
                                song.artist == "null" && song.title == "null"-> ""
                                song.artist == "null" -> song.title
                                song.title == "null" -> song.artist
                                else -> song.artist + "  -  " + song.title
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }
        //  TODO("Fix View Pager changing pages")
    }
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
            val bitmap = (result as? SuccessResult)?.drawable?.let { drawable ->
                (drawable as? BitmapDrawable)?.bitmap
            }

            bitmap?.let {
                Palette.from(it).generate { palette ->
                    val dominantColor = palette?.getDominantColor(defaultColor.toArgb())
                    val color = Color(dominantColor ?: defaultColor.toArgb())

                    backgroundColor = color.copy(
                        red = color.red * dullnessFactor + (1 - dullnessFactor) * 0.5f,
                        green = color.green * dullnessFactor + (1 - dullnessFactor) * 0.5f,
                        blue = color.blue * dullnessFactor + (1 - dullnessFactor) * 0.5f
                    )
                }
            } ?: run {
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
        backdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
    )
}
