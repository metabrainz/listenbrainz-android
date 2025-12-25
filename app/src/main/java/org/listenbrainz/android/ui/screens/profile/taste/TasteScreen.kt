package org.listenbrainz.android.ui.screens.profile.taste

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.shimmerSpec
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserFeedbackEntry
import org.listenbrainz.android.ui.components.ChipItem
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.SelectionChipBar
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.TasteTabUIState
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.screens.profile.listens.headerTextVerticalPadding
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun TasteScreen(
    viewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    feedViewModel: FeedViewModel,
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()

    TasteScreen(
        uiState = uiState,
        socialUiState = socialUiState,
        snackbarState = snackbarState,
        fetchTasteData = {
            viewModel.getUserTasteData(refresh = it)
        },
        playListen = {
            socialViewModel.playListen(it)
        },
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        goToArtistPage = goToArtistPage,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TasteScreen(
    uiState: ProfileUiState,
    socialUiState: SocialUiState,
    snackbarState: SnackbarHostState,
    uriHandler: UriHandler = LocalUriHandler.current,
    fetchTasteData: suspend (Boolean) -> Unit,
    playListen: (TrackMetadata) -> Unit,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    goToArtistPage: (String) -> Unit,
) {
    val lovedHatedState: MutableState<LovedHated> = remember { mutableStateOf(LovedHated.Loved) }

    val lovedHatedCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val pinsCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val isLoading = uiState.tasteTabUIState.isLoading

    val scope = rememberCoroutineScope()

    val isRefreshing = remember(
        isLoading
    ) {
        isLoading
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                fetchTasteData(true)
            }
        }
    )

    val shimmerInstance = rememberShimmer(
        shimmerBounds = ShimmerBounds.View,
        theme = ShimmerTheme(
            animationSpec = infiniteRepeatable(
                animation = shimmerSpec(
                    durationMillis = 300,
                    delayMillis = 800,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            blendMode = BlendMode.DstIn,
            rotation = 10.0f,
            shaderColors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 1.00f),
                Color.White.copy(alpha = 0.25f),
            ),
            shaderColorStops = listOf(
                0.0f,
                0.5f,
                1.0f,
            ),
            shimmerWidth = 500.dp,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            item {
                val lovedHatedChips = listOf(
                    ChipItem(
                        id = LovedHated.Loved.name,
                        label = LovedHated.Loved.name,
                        icon = painterResource(id = R.drawable.heart)
                    ),
                    ChipItem(
                        id = LovedHated.Hated.name,
                        label = LovedHated.Hated.name,
                        icon = rememberVectorPainter(Icons.Default.HeartBroken)
                    )
                )

                SelectionChipBar(
                    items = lovedHatedChips,
                    selectedItemId = when (lovedHatedState.value) {
                        LovedHated.Loved -> LovedHated.Loved.name
                        LovedHated.Hated -> LovedHated.Hated.name
                    },
                    onItemSelected = { data ->
                        lovedHatedState.value = when (data.id) {
                            LovedHated.Loved.name -> LovedHated.Loved
                            LovedHated.Hated.name -> LovedHated.Hated
                            else -> LovedHated.Loved
                        }
                    }
                )
            }
            if (isRefreshing) {
                item {
                    Spacer(modifier = Modifier.height(3.dp))
                }
                items(3) {
                    ShimmerLovedHatedItem(shimmerInstance)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                itemsIndexed(
                    items = when (lovedHatedState.value) {
                        LovedHated.Loved -> when (lovedHatedCollapsibleState.value) {
                            true -> uiState.tasteTabUIState.lovedSongs?.feedback?.take(5)
                                ?: listOf()

                            false -> uiState.tasteTabUIState.lovedSongs?.feedback ?: listOf()
                        }

                        LovedHated.Hated -> when (lovedHatedCollapsibleState.value) {
                            true -> uiState.tasteTabUIState.hatedSongs?.feedback?.take(5)
                                ?: listOf()

                            false -> uiState.tasteTabUIState.hatedSongs?.feedback ?: listOf()
                        }
                    }
                ) { index, feedback ->
                    val metadata = feedback.toMetadata()
                    ListenCardSmallDefault(
                        modifier = Modifier
                            .padding(
                                horizontal = ListenBrainzTheme.paddings.horizontal,
                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                            ),
                        metadata = metadata,
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = feedback.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = feedback.trackMetadata?.mbidMapping?.caaId
                        ),
                        onDropdownError = { error ->
                            snackbarState.showSnackbar(error.toast)
                        },
                        onDropdownSuccess = { message ->
                            snackbarState.showSnackbar(message)
                        },
                        goToArtistPage = goToArtistPage
                    ) {
                        if (feedback.trackMetadata != null) {
                            playListen(feedback.trackMetadata)
                        }
                    }
                }
            }
            item {
                if ((uiState.tasteTabUIState.lovedSongs?.count
                        ?: 0) > 5 || (uiState.tasteTabUIState.hatedSongs?.count ?: 0) > 5
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadMoreButton(
                            modifier = Modifier.padding(16.dp),
                            state = lovedHatedCollapsibleState.value
                        ) {
                            lovedHatedCollapsibleState.value = !lovedHatedCollapsibleState.value
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            item {
                val pinnedRecordings = when (pinsCollapsibleState.value) {
                    true -> uiState.tasteTabUIState.pins?.pinnedRecordings?.take(5) ?: listOf()
                    false -> uiState.tasteTabUIState.pins?.pinnedRecordings ?: listOf()
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        modifier = Modifier.headerTextVerticalPadding(),
                        text = "Pins",
                        fontSize = 22.sp,
                    )

                    if (isRefreshing) {
                        Column {
                            repeat(4) {
                                ShimmerPinsItem(shimmerInstance)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    } else {
                        pinnedRecordings.mapIndexed { index, recording: PinnedRecording ->
                            val metadata = recording.toMetadata()
                            ListenCardSmallDefault(
                                blurbContent = if (!recording.blurbContent.isNullOrBlank()) {
                                    { modifier ->
                                        Text(
                                            modifier = modifier,
                                            text = recording.blurbContent
                                        )
                                    }
                                } else null,
                                modifier = Modifier
                                    .padding(
                                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                                    ),
                                metadata = metadata,
                                coverArtUrl = getCoverArtUrl(
                                    caaReleaseMbid = recording.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                    caaId = recording.trackMetadata?.mbidMapping?.caaId
                                ),
                                onDropdownError = { error ->
                                    snackbarState.showSnackbar(error.toast)
                                },
                                onDropdownSuccess = { message ->
                                    snackbarState.showSnackbar(message)
                                },
                                goToArtistPage = goToArtistPage
                            ) {
                                if (recording.trackMetadata != null) {
                                    playListen(recording.trackMetadata)
                                }
                            }

                        }
                    }
                }

            }
            item {
                if ((uiState.tasteTabUIState.pins?.count ?: 0) > 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LoadMoreButton(
                            modifier = Modifier.padding(16.dp),
                            state = pinsCollapsibleState.value
                        ) {
                            pinsCollapsibleState.value = !pinsCollapsibleState.value
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1,
            state = pullRefreshState
        )
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)
    SuccessBar(
        resId = socialUiState.successMsgId,
        onMessageShown = onMessageShown,
        snackbarState = snackbarState
    )
}

@Composable
fun ShimmerLovedHatedItem(shimmer: Shimmer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(ListenBrainzTheme.sizes.listenCardHeight)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(72.dp)
                .width(60.dp)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(
                        topStart = 6.dp,
                        bottomStart = 6.dp
                    )
                )
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(end = 26.dp)
                .width(80.dp)
                .height(10.dp)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun ShimmerPinsItem(shimmer: Shimmer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ListenBrainzTheme.sizes.listenCardHeight)
            .background(
                Color.Gray.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(72.dp)
                .width(60.dp)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(
                        topStart = 6.dp,
                        bottomStart = 6.dp
                    )
                )
        )
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(10.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .shimmer(shimmer)
                    .background(
                        Color.Gray.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(end = 26.dp)
                .width(80.dp)
                .height(10.dp)
                .shimmer(shimmer)
                .background(
                    Color.Gray.copy(alpha = 0.8f),
                    RoundedCornerShape(2.dp)
                )
        )
    }
}

@PreviewLightDark
@Composable
private fun TasteScreenPreview() {
    // Create mock data for loved songs
    val mockLovedSongs = UserFeedback(
        count = 10,
        feedback = listOf(
            UserFeedbackEntry(
                created = 1234567890,
                recordingMBID = "mbid1",
                score = 1,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "The Beatles",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-1"),
                        artists = listOf(
                            FeedListenArtist("The Beatles", "artist-mbid-1", null)
                        ),
                        caaId = 123456L,
                        caaReleaseMbid = "release-mbid-1",
                        recordingMbid = "recording-mbid-1",
                        recordingName = "Yesterday",
                        releaseMbid = "release-mbid-1"
                    ),
                    releaseName = "Help!",
                    trackName = "Yesterday"
                )
            ),
            UserFeedbackEntry(
                created = 1234567891,
                recordingMBID = "mbid2",
                score = 1,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "Pink Floyd",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-2"),
                        artists = listOf(
                            FeedListenArtist("Pink Floyd", "artist-mbid-2", null)
                        ),
                        caaId = 789012L,
                        caaReleaseMbid = "release-mbid-2",
                        recordingMbid = "recording-mbid-2",
                        recordingName = "Comfortably Numb",
                        releaseMbid = "release-mbid-2"
                    ),
                    releaseName = "The Wall",
                    trackName = "Comfortably Numb"
                )
            ),
            UserFeedbackEntry(
                created = 1234567892,
                recordingMBID = "mbid3",
                score = 1,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "Led Zeppelin",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-3"),
                        artists = listOf(
                            FeedListenArtist("Led Zeppelin", "artist-mbid-3", null)
                        ),
                        caaId = 345678L,
                        caaReleaseMbid = "release-mbid-3",
                        recordingMbid = "recording-mbid-3",
                        recordingName = "Stairway to Heaven",
                        releaseMbid = "release-mbid-3"
                    ),
                    releaseName = "Led Zeppelin IV",
                    trackName = "Stairway to Heaven"
                )
            )
        )
    )

    // Create mock data for hated songs
    val mockHatedSongs = UserFeedback(
        count = 3,
        feedback = listOf(
            UserFeedbackEntry(
                created = 1234567893,
                recordingMBID = "mbid4",
                score = -1,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "Artist A",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-4"),
                        artists = listOf(
                            FeedListenArtist("Artist A", "artist-mbid-4", null)
                        ),
                        caaId = null,
                        caaReleaseMbid = null,
                        recordingMbid = "recording-mbid-4",
                        recordingName = "Annoying Song",
                        releaseMbid = null
                    ),
                    releaseName = "Bad Album",
                    trackName = "Annoying Song"
                )
            )
        )
    )

    // Create mock data for pinned recordings
    val mockPins = AllPinnedRecordings(
        pinnedRecordings = listOf(
            PinnedRecording(
                created = 123456894,
                rowId = 1,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "Queen",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-5"),
                        artists = listOf(
                            FeedListenArtist("Queen", "artist-mbid-5", null)
                        ),
                        caaId = 901234L,
                        caaReleaseMbid = "release-mbid-5",
                        recordingMbid = "recording-mbid-5",
                        recordingName = "Bohemian Rhapsody",
                        releaseMbid = "release-mbid-5"
                    ),
                    releaseName = "A Night at the Opera",
                    trackName = "Bohemian Rhapsody"
                ),
                blurbContent = "This is my favorite song of all time!"
            ),
            PinnedRecording(
                created = 1234567895,
                rowId = 2,
                trackMetadata = TrackMetadata(
                    additionalInfo = null,
                    artistName = "David Bowie",
                    mbidMapping = MbidMapping(
                        artistMbids = listOf("artist-mbid-6"),
                        artists = listOf(
                            FeedListenArtist("David Bowie", "artist-mbid-6", null)
                        ),
                        caaId = 567890L,
                        caaReleaseMbid = "release-mbid-6",
                        recordingMbid = "recording-mbid-6",
                        recordingName = "Space Oddity",
                        releaseMbid = "release-mbid-6"
                    ),
                    releaseName = "Space Oddity",
                    trackName = "Space Oddity"
                ),
                blurbContent = "A timeless classic that never gets old."
            )
        ),
        totalCount = 2,
        userName = "preview_user",
        count = 2,
        offset = 0
    )

    // Create the UI state with mock data
    val mockProfileUiState = ProfileUiState(
        isSelf = true,
        tasteTabUIState = TasteTabUIState(
            isLoading = false,
            lovedSongs = mockLovedSongs,
            hatedSongs = mockHatedSongs,
            pins = mockPins
        )
    )

    val mockSocialUiState = SocialUiState()

    PreviewSurface {
        TasteScreen(
            uiState = mockProfileUiState,
            socialUiState = mockSocialUiState,
            snackbarState = remember { SnackbarHostState() },
            fetchTasteData = {},
            playListen = {},
            onErrorShown = {},
            onMessageShown = {},
            goToArtistPage = {}
        )
    }
}