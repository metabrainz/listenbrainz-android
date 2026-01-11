package org.listenbrainz.android.ui.screens.brainzplayer

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.ui.screens.brainzplayer.ui.components.basicMarquee
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.ListeningNowUIState
import org.listenbrainz.android.viewmodel.ListeningNowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningNowScreen(
    backdropScaffoldState: BackdropScaffoldState,
    viewModel: ListeningNowViewModel = koinViewModel(),
    gradientBox: @Composable () -> Unit
) {
    val listeningNowUIState by viewModel.listeningNowUIState.collectAsState()
    val scope = rememberCoroutineScope()
    val onNavigateBack: () -> Unit = {
        scope.launch {
            backdropScaffoldState.reveal()
        }
    }

    if (backdropScaffoldState.isConcealed) {
        BackHandler {
            onNavigateBack()
        }
    }

    ListeningNowLayout(
        uiState = listeningNowUIState,
        onNavigateBack = onNavigateBack,
        isFullScreen = backdropScaffoldState.currentValue == backdropScaffoldState.targetValue,
        gradientBox = gradientBox
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningNowLayout(
    isFullScreen: Boolean = false,
    uiState: ListeningNowUIState,
    onNavigateBack: () -> Unit,
    gradientBox: @Composable () -> Unit
) {
    val cornerSize = if(isFullScreen) 0.dp else 32.dp
    val titleColor =  ListenBrainzTheme.colorScheme.listenText
    val artistColor = ListenBrainzTheme.colorScheme.text

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(cornerSize, cornerSize, 0.dp, 0.dp))
    ) {
        gradientBox()
        if (uiState.song != null) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Listening Now",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = titleColor,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = uiState.imageURL,
                    contentDescription = "Album artwork",
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_erroralbumart),
                    error = painterResource(id = R.drawable.ic_erroralbumart),
                    onLoading = {
                        Log.d("ListeningNowScreen", "Loading image...")
                    },
                    onError = {
                        Log.d("ListeningNowScreen", "Error loading image" )
                    },
                    onSuccess = {
                        Log.d("ListeningNowScreen", "Image loaded successfully" )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = uiState.song.trackMetadata?.trackName ?: "--",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .basicMarquee()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.song.trackMetadata?.artistName ?: "--",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = artistColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .basicMarquee()
                )

                uiState.song.trackMetadata?.releaseName?.let { albumName ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = albumName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = artistColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .basicMarquee()
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No song currently playing",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = "Navigate Back",
            tint = titleColor,
            modifier = Modifier
                .clickable {
                    onNavigateBack()
                }
                .statusBarsPadding()
                .padding(
                    start = 28.dp,
                    top = 18.dp
                )
        )
    }
}


@Composable
fun ListeningNowCard(
    uiState: ListeningNowUIState,
    isLandscape: Boolean,
    coroutineScope: CoroutineScope,
    backdropScaffoldState: BackdropScaffoldState,
    modifier: Modifier = Modifier
) {
    if (uiState.song != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(ListenBrainzTheme.colorScheme.level2)
                .clickable {
                    coroutineScope.launch {
                        backdropScaffoldState.conceal()
                    }
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uiState.imageURL,
                contentDescription = "Album artwork",
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 4.dp)
                    .size(if (isLandscape) 30.dp else 45.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_erroralbumart),
                error = painterResource(id = R.drawable.ic_erroralbumart)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Listening Now",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                ListeningNowCardSongInfo(
                    currentlyPlayingSong = uiState.song,
                    titleColor = ListenBrainzTheme.colorScheme.text,
                    artistColor = ListenBrainzTheme.colorScheme.text
                )
            }
        }
    }
}

@Composable
fun ListeningNowCardSongInfo(
    currentlyPlayingSong: Listen,
    titleColor: Color,
    artistColor: Color
) {
    val artist = currentlyPlayingSong.trackMetadata?.artistName
    val title = currentlyPlayingSong.trackMetadata?.trackName

    Text(
        text = when {
            artist == null && title == null -> ""
            artist == null -> title
            title == null -> artist
            else -> "$artist  -  $title"
        }.orEmpty(),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
        color = titleColor,
        modifier = Modifier.basicMarquee(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview(showBackground = true)
@Composable
fun ListeningNowLayoutPreview() {
    ListenBrainzTheme {
        ListeningNowLayout(
            uiState = ListeningNowUIState(
                song = Listen(
                    insertedAt = 1234567890,
                    listenedAt = 1234567890,
                    recordingMsid = "test-msid",
                    trackMetadata = TrackMetadata(
                        additionalInfo = AdditionalInfo(),
                        artistName = "The Beatles",
                        mbidMapping = null,
                        releaseName = "Come Together - Remastered",
                        trackName = "Come Together"
                    ),
                    userName = "testuser",
                    coverArt = null
                ),
            ),
            onNavigateBack = {},
            gradientBox = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListeningNowCardPreview() {
    ListenBrainzTheme {
        val coroutineScope = rememberCoroutineScope()
        ListeningNowCard(
            uiState = ListeningNowUIState(
                song = Listen(
                    insertedAt = 1234567890,
                    listenedAt = 1234567890,
                    recordingMsid = "test-msid",
                    trackMetadata = TrackMetadata(
                        additionalInfo = AdditionalInfo(),
                        artistName = "The Beatles",
                        mbidMapping = null,
                        releaseName = "Abbey Road - Remastered",
                        trackName = "Come Together"
                    ),
                    userName = "testuser",
                    coverArt = null
                ),
            ),
            isLandscape = false,
            coroutineScope = coroutineScope,
            backdropScaffoldState = rememberBackdropScaffoldState(
                BackdropValue.Revealed
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListeningNowCardLandscapePreview() {
    ListenBrainzTheme {
        val coroutineScope = rememberCoroutineScope()
        ListeningNowCard(
            uiState = ListeningNowUIState(
                song = Listen(
                    insertedAt = 1234567890,
                    listenedAt = 1234567890,
                    recordingMsid = "test-msid",
                    trackMetadata = TrackMetadata(
                        additionalInfo = AdditionalInfo(),
                        artistName = "Pink Floyd",
                        mbidMapping = null,
                        releaseName = "The Dark Side of the Moon",
                        trackName = "Money"
                    ),
                    userName = "testuser",
                    coverArt = null
                ),
            ),
            isLandscape = true,
            coroutineScope = coroutineScope,
            backdropScaffoldState = rememberBackdropScaffoldState(
                BackdropValue.Revealed
            ),
        )
    }
}
