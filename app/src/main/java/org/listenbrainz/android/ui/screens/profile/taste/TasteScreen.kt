package org.listenbrainz.android.ui.screens.profile.taste

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.model.user.AllPinnedRecordings
import org.listenbrainz.android.model.user.UserFeedback
import org.listenbrainz.android.model.user.UserFeedbackEntry
import org.listenbrainz.android.ui.components.ChipItem
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.SelectionChipBar
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.TasteTabUIState
import org.listenbrainz.android.ui.screens.profile.listens.Dialogs
import org.listenbrainz.android.ui.screens.profile.listens.ListenDialogBundleKeys
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
    val feedUiState by feedViewModel.uiState.collectAsState()

    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }

    TasteScreen(
        uiState = uiState,
        socialUiState = socialUiState,
        feedUiState = feedUiState,
        snackbarState = snackbarState,
        dropdownItemIndex = dropdownItemIndex,
        playListen = {
            socialViewModel.playListen(it)
        },
        onPin = { metadata, blurbContent ->
            socialViewModel.pin(metadata, blurbContent)
            dropdownItemIndex.value = null
        },
        onRecommend = { metadata ->
            socialViewModel.recommend(metadata)
            dropdownItemIndex.value = null
        },
        searchUsers = { query ->
            feedViewModel.searchUser(query)
        },
        isCritiqueBrainzLinked = {
            feedViewModel.isCritiqueBrainzLinked()
        },
        onReview = { type, blurbContent, rating, locale, metadata ->
            socialViewModel.review(metadata, type, blurbContent, rating, locale)
        },
        onPersonallyRecommend = { metadata, users, blurbContent ->
            socialViewModel.personallyRecommend(metadata, users, blurbContent)
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

@Composable
fun TasteScreen(
    uiState: ProfileUiState,
    socialUiState: SocialUiState,
    feedUiState: FeedUiState,
    snackbarState: SnackbarHostState,
    uriHandler: UriHandler = LocalUriHandler.current,
    dropdownItemIndex: MutableState<Int?>,
    playListen: (TrackMetadata) -> Unit,
    onPin: (metadata: Metadata, blurbContent: String) -> Unit,
    onRecommend: (metadata: Metadata) -> Unit,
    searchUsers: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String, metadata: Metadata) -> Unit,
    onPersonallyRecommend: (metadata: Metadata, users: List<String>, blurbContent: String) -> Unit,
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

    val dialogsState = rememberDialogsState()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

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
        itemsIndexed(
            items = when (lovedHatedState.value) {
                LovedHated.Loved -> when (lovedHatedCollapsibleState.value) {
                    true -> uiState.tasteTabUIState.lovedSongs?.feedback?.take(5) ?: listOf()
                    false -> uiState.tasteTabUIState.lovedSongs?.feedback ?: listOf()
                }

                LovedHated.Hated -> when (lovedHatedCollapsibleState.value) {
                    true -> uiState.tasteTabUIState.hatedSongs?.feedback?.take(5) ?: listOf()
                    false -> uiState.tasteTabUIState.hatedSongs?.feedback ?: listOf()
                }
            }
        ) { index, feedback ->
            val metadata = Metadata(trackMetadata = feedback.trackMetadata)
            ListenCardSmall(
                modifier = Modifier
                    .padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                trackName = feedback.trackMetadata?.trackName ?: "",
                artists = feedback.trackMetadata?.mbidMapping?.artists ?: listOf(
                    FeedListenArtist(feedback.trackMetadata?.artistName ?: "", null, "")
                ),
                coverArtUrl = getCoverArtUrl(
                    caaReleaseMbid = feedback.trackMetadata?.mbidMapping?.caaReleaseMbid,
                    caaId = feedback.trackMetadata?.mbidMapping?.caaId
                ),
                onDropdownIconClick = {
                    dropdownItemIndex.value = index
                },
                dropDown = {
                    SocialDropdown(
                        isExpanded = dropdownItemIndex.value == index,
                        onDismiss = {
                            dropdownItemIndex.value = null
                        },
                        metadata = metadata,
                        onRecommend = { onRecommend(metadata) },
                        onPersonallyRecommend = {
                            dialogsState.activateDialog(
                                Dialog.PERSONAL_RECOMMENDATION,
                                ListenDialogBundleKeys.listenDialogBundle(0, index)
                            )
                            dropdownItemIndex.value = null
                        },
                        onReview = {
                            dialogsState.activateDialog(
                                Dialog.REVIEW,
                                ListenDialogBundleKeys.listenDialogBundle(0, index)
                            )
                            dropdownItemIndex.value = null
                        },
                        onPin = {
                            dialogsState.activateDialog(
                                Dialog.PIN,
                                ListenDialogBundleKeys.listenDialogBundle(0, index)
                            )
                            dropdownItemIndex.value = null
                        },
                        onOpenInMusicBrainz = {
                            try {
                                uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                            } catch (e: Error) {
                                scope.launch {
                                    snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                }
                            }
                            dropdownItemIndex.value = null
                        }

                    )
                },
                goToArtistPage = goToArtistPage
            ) {
                if (feedback.trackMetadata != null) {
                    playListen(feedback.trackMetadata)
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

                pinnedRecordings.mapIndexed { index, recording: PinnedRecording ->
                    val metadata = Metadata(trackMetadata = recording.trackMetadata)
                    ListenCardSmall(
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
                        trackName = recording.trackMetadata?.trackName ?: "",
                        artists = recording.trackMetadata?.mbidMapping?.artists ?: listOf(
                            FeedListenArtist(
                                recording.trackMetadata?.artistName ?: "",
                                null,
                                ""
                            )
                        ),
                        coverArtUrl = getCoverArtUrl(
                            caaReleaseMbid = recording.trackMetadata?.mbidMapping?.caaReleaseMbid,
                            caaId = recording.trackMetadata?.mbidMapping?.caaId
                        ),
                        onDropdownIconClick = {
                            dropdownItemIndex.value = index
                        },
                        dropDown = {
                            SocialDropdown(
                                isExpanded = dropdownItemIndex.value == index,
                                onDismiss = {
                                    dropdownItemIndex.value = null
                                },
                                metadata = metadata,
                                onRecommend = { onRecommend(metadata) },
                                onPersonallyRecommend = {
                                    dialogsState.activateDialog(
                                        Dialog.PERSONAL_RECOMMENDATION,
                                        ListenDialogBundleKeys.listenDialogBundle(0, index)
                                    )
                                    dropdownItemIndex.value = null
                                },
                                onReview = {
                                    dialogsState.activateDialog(
                                        Dialog.REVIEW,
                                        ListenDialogBundleKeys.listenDialogBundle(0, index)
                                    )
                                    dropdownItemIndex.value = null
                                },
                                onPin = {
                                    dialogsState.activateDialog(
                                        Dialog.PIN,
                                        ListenDialogBundleKeys.listenDialogBundle(0, index)
                                    )
                                    dropdownItemIndex.value = null
                                },
                                onOpenInMusicBrainz = {
                                    try {
                                        uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                                    } catch (e: Error) {
                                        scope.launch {
                                            snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                        }
                                    }
                                    dropdownItemIndex.value = null
                                }

                            )
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

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)
    SuccessBar(
        resId = socialUiState.successMsgId,
        onMessageShown = onMessageShown,
        snackbarState = snackbarState
    )

    Dialogs(
        deactivateDialog = {
            dialogsState.deactivateDialog()
        },
        currentDialog = dialogsState.currentDialog,
        currentIndex = dialogsState.metadata?.getInt(ListenDialogBundleKeys.EVENT_INDEX.name),
        listens = uiState.listensTabUiState.recentListens ?: listOf(),
        onPin = { metadata, blurbContent -> onPin(metadata, blurbContent) },
        searchUsers = { query -> searchUsers(query) },
        feedUiState = feedUiState,
        isCritiqueBrainzLinked = isCritiqueBrainzLinked,
        onReview = { type, blurbContent, rating, locale, metadata ->
            onReview(
                type,
                blurbContent,
                rating,
                locale,
                metadata
            )
        },
        onPersonallyRecommend = { metadata, users, blurbContent ->
            onPersonallyRecommend(
                metadata,
                users,
                blurbContent
            )
        },
        snackbarState = snackbarState,
        socialUiState = socialUiState
    )
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
    val mockFeedUiState = FeedUiState()

    PreviewSurface {
        TasteScreen(
            uiState = mockProfileUiState,
            socialUiState = mockSocialUiState,
            feedUiState = mockFeedUiState,
            snackbarState = remember { SnackbarHostState() },
            dropdownItemIndex = remember { mutableStateOf(null) },
            playListen = {},
            onPin = { _, _ -> },
            onRecommend = {},
            searchUsers = {},
            isCritiqueBrainzLinked = { null },
            onReview = { _, _, _, _, _ -> },
            onPersonallyRecommend = { _, _, _ -> },
            onErrorShown = {},
            onMessageShown = {},
            goToArtistPage = {}
        )
    }
}