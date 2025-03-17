package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.components.CoverArtComposable
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.screens.profile.createdforyou.formatDateLegacy
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.util.Utils.formatDurationSeconds
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.util.Utils.removeHtmlTags
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistMBID: String,
    playlistViewModel: PlaylistDataViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by playlistViewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.playlistDetailUIState.isRefreshing,
        onRefresh = {
            playlistViewModel.getDataInPlaylistScreen(playlistMBID, isRefresh = true)
        }
    )
    val addTrackSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var isEditPlaylistBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playlistViewModel.getDataInPlaylistScreen(playlistMBID)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        AnimatedContent(
            uiState.playlistDetailUIState.isLoading and !uiState.playlistDetailUIState.isRefreshing,
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        LoadingAnimation()
                    }

                } else {
                    if (uiState.playlistDetailUIState.playlistData != null) {
                        PlaylistDetailContent(
                            playlistDetailUIState = uiState.playlistDetailUIState,
                            goToArtistPage = goToArtistPage,
                            onTrackClick = {
                                it.toMetadata().trackMetadata?.let { it1 ->
                                    socialViewModel.playListen(
                                        it1
                                    )
                                }
                            },
                            showsnackbar = {
                                scope.launch {
                                    snackbarState.showSnackbar(it)
                                }
                            },
                            onAddTrackClick = {
                                playlistViewModel.changeAddTrackBottomSheetState(true)
                            },
                            onEditPlaylistClick = {
                                isEditPlaylistBottomSheetVisible = true
                            },
                            goToUserPage = goToUserPage,
                            onDuplicatePlaylistClick = {
                                playlistViewModel.duplicatePlaylist(playlistMBID)
                            },
                            onSharePlaylistClick = {
                                if (uiState.playlistDetailUIState.playlistData?.identifier != null)
                                    Utils.shareLink(
                                        context,
                                        uiState.playlistDetailUIState.playlistData?.identifier!!
                                    )
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HelperText(
                                modifier = Modifier.padding(16.dp),
                                text = "Couldn't load the playlist data"
                            )
                            RetryButton() {
                                playlistViewModel.getDataInPlaylistScreen(playlistMBID)
                            }
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = uiState.playlistDetailUIState.isRefreshing,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1,
            state = pullRefreshState
        )

        if (isEditPlaylistBottomSheetVisible)
            ModalBottomSheet(
                onDismissRequest = {
                    isEditPlaylistBottomSheetVisible = false
                },
                sheetState = sheetState,
                modifier = Modifier.statusBarsPadding(),
            ) {
                CreateEditPlaylistScreen(
                    viewModel = playlistViewModel,
                    snackbarHostState = snackbarState,
                    bottomSheetState = sheetState,
                    mbid = playlistMBID
                ) {
                    scope.launch {
                        sheetState.hide()
                        isEditPlaylistBottomSheetVisible = false
                    }
                }
            }

        if (uiState.playlistDetailUIState.isAddTrackBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    playlistViewModel.changeAddTrackBottomSheetState(false)
                },
                sheetState = addTrackSheetState,
                modifier = Modifier.statusBarsPadding()
            ) {
                AddTrackToPlaylist(
                    modifier = Modifier.fillMaxSize(),
                    playlistDetailUIState = uiState.playlistDetailUIState,
                    onTrackSelect = { recordingData ->
                        playlistViewModel.addTrackToPlaylist(
                            recordingData
                        )
                        playlistViewModel.changeAddTrackBottomSheetState(false)
                    },
                    onQueryChange = {
                        playlistViewModel.queryRecordings(it)
                    },
                    onDismiss = {
                        playlistViewModel.changeAddTrackBottomSheetState(false)
                        playlistViewModel.queryRecordings("")
                    }
                )
            }
        }

        ErrorBar(socialUiState.error, socialViewModel::clearErrorFlow)
        SuccessBar(socialUiState.successMsgId, socialViewModel::clearMsgFlow, snackbarState)
        ErrorBar(uiState.error, playlistViewModel::clearErrorFlow)
        SuccessBar(uiState.successMsg, playlistViewModel::clearMsgFlow, snackbarState)
    }
}

@Composable
private fun PlaylistDetailContent(
    playlistDetailUIState: PlaylistDetailUIState,
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String) -> Unit,
    showsnackbar: (String) -> Unit,
    onAddTrackClick: () -> Unit,
    onTrackClick: (PlaylistTrack) -> Unit,
    onEditPlaylistClick: () -> Unit,
    onSharePlaylistClick: () -> Unit,
    onDuplicatePlaylistClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            item {
                PlaylistCard(
                    title = playlistDetailUIState.playlistData?.title ?: "",
                    isPublic = playlistDetailUIState.playlistData?.extension?.playlistExtensionData?.public
                        ?: false,
                    creatorName = playlistDetailUIState.playlistData?.creator ?: "",
                    onCreatorClick = {
                        if (playlistDetailUIState.playlistData?.creator != null)
                            goToUserPage(playlistDetailUIState.playlistData.creator)
                    },
                    trackCount = playlistDetailUIState.playlistData?.track?.size ?: 0,
                    totalLengthSeconds = playlistDetailUIState.playlistData?.track?.sumOf {
                        it.duration ?: 0
                    } ?: 0,
                    lastModified = formatDateLegacy(
                        playlistDetailUIState.playlistData?.extension?.playlistExtensionData?.lastModifiedAt
                            ?: ""
                    ),
                    created = formatDateLegacy(playlistDetailUIState.playlistData?.date ?: ""),
                    description = removeHtmlTags(
                        playlistDetailUIState.playlistData?.annotation ?: ""
                    ),
                    coverArt = playlistDetailUIState.playlistData?.coverArt
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    //Duplicate button
                    PlaylistButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        isCircular = true,
                        onClick = {
                            onDuplicatePlaylistClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.playlist_duplicate_svg),
                            contentDescription = "Duplicate Playlist",
                            tint = Color.White
                        )
                    }

                    if (playlistDetailUIState.isUserPlaylistOwner) {
                        PlaylistButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            isCircular = true,
                            onClick = {
                                onEditPlaylistClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }
                    }

                    PlaylistButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        isCircular = true,
                        onClick = {
                            onSharePlaylistClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.playlist_share_btn),
                            contentDescription = "Share playlist",
                            modifier = Modifier.size(30.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            if (playlistDetailUIState.isUserPlaylistOwner) {
                item {
                    AddTrackCard(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        onClick = {
                            onAddTrackClick()
                        }
                    )
                }
            }
            items(playlistDetailUIState.playlistData?.track?.size ?: 0) { index ->
                val playlist = playlistDetailUIState.playlistData?.track?.get(index)
                if (playlist != null) {
                    ListenCardSmallDefault(
                        modifier = Modifier.padding(
                            horizontal = ListenBrainzTheme.paddings.horizontal,
                            vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                        ),
                        metadata = (playlist.toMetadata()),
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = playlist.extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                            caaId = playlist.extension.trackExtensionData.additionalMetadata.caaId
                        ),
                        onDropdownSuccess = { messsage ->
                            showsnackbar(messsage)
                        },
                        onDropdownError = { error ->
                            showsnackbar(error.toast)
                        },
                        goToArtistPage = goToArtistPage,
                        onClick = {
                            onTrackClick(playlist)
                        },
                        trailingContent = {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp),
                                text = formatDurationSeconds(playlist.duration?.div(1000) ?: 0),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        enableTrailingContent = true
                    )


                }
            }
            item {
                if (playlistDetailUIState.playlistData?.track?.size == 0) {
                    HelperText(
                        modifier = Modifier.padding(16.dp),
                        text = "No tracks found"
                    )
                }
            }
        }

    }
}

@Composable
fun PlaylistCard(
    title: String,
    isPublic: Boolean,
    creatorName: String,
    onCreatorClick: () -> Unit,
    trackCount: Int,
    totalLengthSeconds: Int,
    lastModified: String,
    created: String,
    description: String,
    coverArt: String?,
    modifier: Modifier = Modifier
) {
    val themeColors = ListenBrainzTheme.colorScheme
    val totalMinutes = totalLengthSeconds / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    var isReadMoreEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        ListenBrainzTheme.colorScheme.background,
                        ListenBrainzTheme.colorScheme.level2.copy(0.2f),
                        ListenBrainzTheme.colorScheme.level2.copy(0.4f),
                        ListenBrainzTheme.colorScheme.level2.copy(0.6f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverArtComposable(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    coverArt = coverArt,
                    maxGridSize = 3
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .heightIn(min = 130.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = title,
                            color = themeColors.listenText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            Text(
                                text = if (isPublic) "Public Playlist by " else "Private Playlist by ",
                                color = themeColors.text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                maxLines = 1
                            )
                            Text(
                                text = creatorName,
                                color = themeColors.listenText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.clickable { onCreatorClick() },
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        Text(
                            text = "$trackCount tracks - $hours hours $minutes min",
                            color = themeColors.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "Last Modified: $lastModified",
                            color = themeColors.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = "Created: $created",
                            color = themeColors.text,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            val isTextOverflowing = textLayoutResult?.hasVisualOverflow ?: false

            if (description.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isReadMoreEnabled = !isReadMoreEnabled }
                ) {
                    Text(
                        text = description,
                        color = themeColors.text,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        maxLines = if (isReadMoreEnabled) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult = it }
                    )

                    if (isTextOverflowing && !isReadMoreEnabled) {
                        Text(
                            text = "Read more",
                            color = themeColors.listenText,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PlaylistButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isCircular: Boolean = false,
    enabled: Boolean = true,
    buttonContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .then(if (isCircular) Modifier.size(48.dp) else Modifier)
            .background(
                color = if (enabled) lb_purple else Color.Gray,
                shape = if (isCircular) CircleShape else RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp), // Padding inside the button
        contentAlignment = Alignment.Center
    ) {
        buttonContent()
    }
}


@Composable
fun HelperText(
    modifier: Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f)
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistDetailScreenPreview() {
    ListenBrainzTheme {
        PlaylistDetailContent(
            playlistDetailUIState = PlaylistDetailUIState(
                playlistData = PlaylistData(
                    title = "Playlist Title",
                    creator = "Creator Name",
                    date = "2021-09-01",
                    annotation = "Playlist Description",
                    coverArt = "coverArt"
                )
            ),
            goToArtistPage = {},
            onTrackClick = {},
            showsnackbar = {},
            onAddTrackClick = {},
            onEditPlaylistClick = {},
            goToUserPage = {},
            onDuplicatePlaylistClick = {},
            onSharePlaylistClick = {}
        )
    }
}