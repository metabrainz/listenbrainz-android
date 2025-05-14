package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.ui.components.CoverArtComposable
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.BaseDialog
import org.listenbrainz.android.ui.components.dialogs.DialogNegativeButton
import org.listenbrainz.android.ui.components.dialogs.DialogPositiveButton
import org.listenbrainz.android.ui.components.dialogs.DialogText
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.screens.profile.createdforyou.formatDateLegacy
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.util.Log
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
    var isEditPlaylistBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isDuplicatePlaylistDialogVisible by rememberSaveable { mutableStateOf(false) }

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
                                isDuplicatePlaylistDialogVisible = true
                            },
                            onSharePlaylistClick = {
                                if (uiState.playlistDetailUIState.playlistData?.identifier != null)
                                    Utils.shareLink(
                                        context,
                                        uiState.playlistDetailUIState.playlistData?.identifier!!
                                    )
                            },
                            onPermanentlyMoveTrack = {
                                playlistViewModel.reorderPlaylist(it)
                            },
                            onTemporarilyMoveTrack = { fromIndex, toIndex ->
                                playlistViewModel.temporarilyMoveTrack(fromIndex, toIndex)
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


        CreateEditPlaylistScreen(
            viewModel = playlistViewModel,
            isVisible = isEditPlaylistBottomSheetVisible,
            mbid = playlistMBID
        ) {
            isEditPlaylistBottomSheetVisible = false

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

        if (isDuplicatePlaylistDialogVisible) {
            PlaylistDuplicateConfirmationDialog(
                onDismiss = {
                    isDuplicatePlaylistDialogVisible = false
                },
                onSave = {
                    playlistViewModel.duplicatePlaylist(playlistMBID)
                    isDuplicatePlaylistDialogVisible = false
                }
            )
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
    onDuplicatePlaylistClick: () -> Unit,
    onPermanentlyMoveTrack: (MoveTrack) -> Unit,
    onTemporarilyMoveTrack: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val playlists = playlistDetailUIState.playlistData?.track ?: emptyList()
    val listState = rememberLazyListState()

    //Store the current index of item being dragged
    var draggingItemIndex: Int? by remember {
        mutableStateOf(null)
    }

    //Store the initial index of item being dragged
    var draggingItemInitialIndex by remember {
        mutableIntStateOf(0)
    }
    var targetIndex: Int by remember { mutableIntStateOf(0) }

    var delta: Float by remember {
        mutableFloatStateOf(0f)
    }

    var draggingItem: LazyListItemInfo? by remember {
        mutableStateOf(null)
    }

    val scope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ListenBrainzTheme.colorScheme.userPageGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(key1 = listState) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                                ?.also {
                                    (it.contentType as? DraggableItem)?.let { draggableItem ->
                                        draggingItem = it
                                        draggingItemIndex = draggableItem.index
                                        draggingItemInitialIndex = draggingItemIndex ?: 0
                                    }
                                }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            delta += dragAmount.y

                            val currentDraggingItemIndex =
                                draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                            val currentDraggingItem =
                                draggingItem ?: return@detectDragGesturesAfterLongPress

                            val startOffset = currentDraggingItem.offset + delta
                            val endOffset =
                                currentDraggingItem.offset + currentDraggingItem.size + delta
                            val middleOffset = startOffset + (endOffset - startOffset) / 2

                            val targetItem =
                                listState.layoutInfo.visibleItemsInfo.find { item ->
                                    middleOffset.toInt() in item.offset..item.offset + item.size &&
                                            currentDraggingItem.index != item.index &&
                                            item.contentType is DraggableItem
                                }

                            if (targetItem != null) {
                                targetIndex = (targetItem.contentType as DraggableItem).index
                                onTemporarilyMoveTrack(currentDraggingItemIndex, targetIndex)
                                draggingItemIndex = targetIndex
                                delta += currentDraggingItem.offset - targetItem.offset
                                draggingItem = targetItem
                            } else {
                                val startOffsetToTop =
                                    startOffset - listState.layoutInfo.viewportStartOffset
                                val endOffsetToBottom =
                                    endOffset - listState.layoutInfo.viewportEndOffset
                                val scroll =
                                    when {
                                        startOffsetToTop < 0 -> startOffsetToTop.coerceAtMost(0f)
                                        endOffsetToBottom > 0 -> endOffsetToBottom.coerceAtLeast(0f)
                                        else -> 0f
                                    }
                                val canScrollDown =
                                    currentDraggingItemIndex != playlists.size - 1 && endOffsetToBottom > 0
                                val canScrollUp =
                                    currentDraggingItemIndex != 0 && startOffsetToTop < 0
                                if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                    scope.launch {
                                        listState.scrollBy(scroll)
                                    }
                                }
                            }
                        },
                        onDragEnd = {

                            if (playlistDetailUIState.playlistMBID != null)
                                onPermanentlyMoveTrack(
                                    MoveTrack(
                                        playlistDetailUIState.playlistMBID,
                                        from = draggingItemInitialIndex,
                                        to = targetIndex,
                                        count = 1
                                    )
                                )
                            draggingItem = null
                            draggingItemIndex = null
                            delta = 0f
                        },
                        onDragCancel = {
                            draggingItem = null
                            draggingItemIndex = null
                            delta = 0f
                        },
                    )
                },
            state = listState
        ) {

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
                        .background(brush = ListenBrainzTheme.colorScheme.userPageGradient)
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
            itemsIndexed(
                playlists,
                contentType = { index, _ -> DraggableItem(index = index) }) { index, playlistTrack ->
                val modifier =
                    if (draggingItemIndex == index && playlistDetailUIState.isUserPlaylistOwner) {
                        Modifier
                            .zIndex(1f)
                            .graphicsLayer {
                                translationY = delta
                            }
                    } else {
                        Modifier
                    }
                ListenCardSmallDefault(
                    modifier = modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    metadata = (playlistTrack.toMetadata()),
                    coverArtUrl = getCoverArtUrl(
                        caaReleaseMbid = playlistTrack.extension.trackExtensionData.additionalMetadata.caaReleaseMbid,
                        caaId = playlistTrack.extension.trackExtensionData.additionalMetadata.caaId
                    ),
                    onDropdownSuccess = { messsage ->
                        showsnackbar(messsage)
                    },
                    onDropdownError = { error ->
                        showsnackbar(error.toast)
                    },
                    goToArtistPage = goToArtistPage,
                    onClick = {
                        onTrackClick(playlistTrack)
                    },
                    trailingContent = {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 4.dp),
                            text = formatDurationSeconds(playlistTrack.duration?.div(1000) ?: 0),
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enableTrailingContent = true,
                    preCoverArtContent = if (playlistDetailUIState.isUserPlaylistOwner) { modifier2 ->
                        Icon(
                            modifier = modifier2.padding(horizontal = 4.dp),
                            painter = painterResource(R.drawable.playlist_reorder),
                            tint = ListenBrainzTheme.colorScheme.listenText,
                            contentDescription = "Reorder Icon"
                        )
                    } else null
                )
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
            .background(
                color = ListenBrainzTheme.colorScheme.background
            )
            .padding(top = ListenBrainzTheme.paddings.defaultPadding)
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
                    areImagesClickable = true,
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
                        .animateContentSize()
                ) {
                    Text(
                        text = description,
                        color = themeColors.text.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
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
                    } else if (isTextOverflowing) {
                        Text(
                            text = "Read less",
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
            .clip(if (isCircular) CircleShape else RoundedCornerShape(16.dp))
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
fun PlaylistDuplicateConfirmationDialog(onDismiss: () -> Unit, onSave: () -> Unit) {
    BaseDialog(
        title = {
            DialogText(
                text = "Duplicate Playlist Confirmation",
                bold = true
            )
        },
        content = {
            DialogText(
                text = "Are you sure you want to create a duplicate of this playlist? This action " +
                        "will create an identical copy and will be added to your playlists."
            )
        },
        footer = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                DialogNegativeButton(text = "Cancel") {
                    onDismiss()
                }
                Spacer(modifier = Modifier.width(4.dp))
                DialogPositiveButton(text = "Confirm") {
                    onSave()
                }
            }
        },
        onDismiss = onDismiss,
    )
}

data class DraggableItem(val index: Int)


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
            onSharePlaylistClick = {},
            onPermanentlyMoveTrack = {},
            onTemporarilyMoveTrack = { _, _ -> }
        )
    }
}