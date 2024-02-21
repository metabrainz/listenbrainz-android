package org.listenbrainz.android.ui.screens.listens

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.PersonalRecommendationDialog
import org.listenbrainz.android.ui.components.dialogs.PinDialog
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.screens.profile.UserData
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun ListensScreen(
    viewModel: ListensViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    feedViewModel : FeedViewModel = hiltViewModel(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    snackbarState : SnackbarHostState
) {
    
    val uiState by viewModel.uiState.collectAsState()
    val preferencesUiState by viewModel.preferencesUiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val feedUiState by feedViewModel.uiState.collectAsState()
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }

    ListensScreen(
        scrollRequestState = scrollRequestState,
        onScrollToTop = onScrollToTop,
        uiState = uiState,
        feedUiState = feedUiState,
        preferencesUiState = preferencesUiState,
        updateNotificationServicePermissionStatus = {
            viewModel.updateNotificationServicePermissionStatus()
        },
        dropdownItemIndex = dropdownItemIndex,
        validateUserToken = { token ->
            viewModel.validateUserToken(token)
        },
        setToken = {
            viewModel.setAccessToken(it)
        },
        playListen = {
            socialViewModel.playListen(it)
        },
        snackbarState = snackbarState,
        socialUiState = socialUiState,
        onRecommend = {metadata ->
            socialViewModel.recommend(metadata)
            dropdownItemIndex.value = null
        },
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        onPin = {
            metadata, blurbContent -> socialViewModel.pin(metadata , blurbContent)
            dropdownItemIndex.value = null
        },
        searchUsers = {
            query -> feedViewModel.searchUser(query)
        },
        isCritiqueBrainzLinked = {
             feedViewModel.isCritiqueBrainzLinked()
        },
        onReview = {
            type, blurbContent, rating, locale, metadata ->  socialViewModel.review(metadata , type , blurbContent , rating , locale)
        },
        onPersonallyRecommend = {
            metadata, users, blurbContent ->  socialViewModel.personallyRecommend(metadata, users, blurbContent)
        }
    )
}

private enum class ListenDialogBundleKeys {
    PAGE,
    EVENT_INDEX;
    companion object {
        fun listenDialogBundle(page: Int, eventIndex: Int): Bundle {
            return Bundle().apply {
                putInt(PAGE.name, page)
                putInt(EVENT_INDEX.name, eventIndex)
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListensScreen(
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    uiState: ListensUiState,
    feedUiState: FeedUiState,
    preferencesUiState: PreferencesUiState,
    updateNotificationServicePermissionStatus: () -> Unit,
    dropdownItemIndex : MutableState<Int?>,
    validateUserToken: suspend (String) -> Boolean,
    setToken: (String) -> Unit,
    playListen: (TrackMetadata) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState,
    onRecommend : (metadata : Metadata) -> Unit,
    onErrorShown : () -> Unit,
    onMessageShown : () -> Unit,
    onPin : (metadata : Metadata , blurbContent : String) -> Unit,
    searchUsers: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String, metadata: Metadata) -> Unit,
    onPersonallyRecommend: (metadata: Metadata, users: List<String>, blurbContent: String) -> Unit
) {
    val listState = rememberLazyListState()

    val dialogsState = rememberDialogsState()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    
    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            listState.scrollToItem(0)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        
        LazyColumn(state = listState) {
            item {
                UserData(
                    preferencesUiState,
                    updateNotificationServicePermissionStatus,
                    validateUserToken,
                    setToken
                )
            }
            
            item {
                val pagerState = rememberPagerState { 1 }
                
                // TODO: Figure out the use of ListeningNowOnSpotify. It is hidden for now
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> {
                            uiState.listeningNowUiState.listeningNow.let { listeningNow ->
                                ListeningNowCard(
                                    listeningNow,
                                    Utils.getCoverArtUrl(
                                        caaReleaseMbid = listeningNow?.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                        caaId = listeningNow?.trackMetadata?.mbidMapping?.caaId
                                    )
                                ) {
                                    listeningNow?.let { listen -> playListen(listen.trackMetadata) }
                                }
                            }
                        }
                        
                        1 -> {
                            AnimatedVisibility(
                                visible = uiState.listeningNowUiState.playerState?.track?.name != null,
                                enter = slideInVertically(),
                                exit = slideOutVertically()
                            ) {
                                ListeningNowOnSpotify(
                                    playerState = uiState.listeningNowUiState.playerState,
                                    bitmap = uiState.listeningNowUiState.listeningNowBitmap
                                )
                            }
                        }
                    }
                }
            }
            
            itemsIndexed(items = uiState.listens) { index , listen  ->
                val metadata = Metadata(trackMetadata = listen.trackMetadata)
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    trackName = listen.trackMetadata.trackName,
                    artistName = listen.trackMetadata.artistName,
                    coverArtUrl = Utils.getCoverArtUrl(
                        caaReleaseMbid = listen.trackMetadata.mbidMapping?.caaReleaseMbid,
                        caaId = listen.trackMetadata.mbidMapping?.caaId
                    ),
                    dropDown = {
                        SocialDropdown(
                            isExpanded = dropdownItemIndex.value == index,
                            onDismiss = {
                                dropdownItemIndex.value = null
                            },
                            metadata = metadata,
                            onRecommend = { onRecommend(metadata) },
                            onPersonallyRecommend = {
                                dialogsState.activateDialog(Dialog.PERSONAL_RECOMMENDATION , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                dropdownItemIndex.value = null
                            },
                            onReview = {
                                dialogsState.activateDialog(Dialog.REVIEW , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                dropdownItemIndex.value = null
                            },
                            onPin = {
                                dialogsState.activateDialog(Dialog.PIN , ListenDialogBundleKeys.listenDialogBundle(0, index))
                                dropdownItemIndex.value = null
                            },
                            onOpenInMusicBrainz = {
                                try {
                                    uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                                }
                                catch(e : Error) {
                                    scope.launch {
                                        snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                    }
                                }
                                dropdownItemIndex.value = null
                            }

                        )
                    },
                    enableDropdownIcon = true,
                    onDropdownIconClick = {
                        dropdownItemIndex.value = index
                    }
                ) {
                    playListen(listen.trackMetadata)
                }
            }
        }

        ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown )
        SuccessBar(resId = socialUiState.successMsgId, onMessageShown = onMessageShown, snackbarState = snackbarState)

        Dialogs(
            deactivateDialog = {
                dialogsState.deactivateDialog()
            },
            currentDialog = dialogsState.currentDialog,
            currentIndex = dialogsState.metadata?.getInt(ListenDialogBundleKeys.EVENT_INDEX.name),
            listens = uiState.listens,
            onPin = {metadata, blurbContent ->  onPin(metadata, blurbContent)},
            searchUsers = { query -> searchUsers(query) },
            feedUiState = feedUiState,
            isCritiqueBrainzLinked = isCritiqueBrainzLinked,
            onReview = {type, blurbContent, rating, locale, metadata -> onReview(type, blurbContent, rating, locale, metadata) },
            onPersonallyRecommend = {metadata, users, blurbContent -> onPersonallyRecommend(metadata, users, blurbContent)},
            snackbarState = snackbarState,
            socialUiState = socialUiState
        )
        
        // Loading Animation
        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
    }
}

@Composable
private fun Dialogs(
    deactivateDialog: () -> Unit,
    currentDialog: Dialog,
    feedUiState: FeedUiState,
    currentIndex : Int?,
    listens : List<Listen>,
    onPin : (metadata: Metadata , blurbContent : String) -> Unit,
    searchUsers : (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview : (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String , metadata : Metadata) -> Unit,
    onPersonallyRecommend : (metadata : Metadata , users : List<String> , blurbContent : String) -> Unit,
    snackbarState: SnackbarHostState,
    socialUiState: SocialUiState
) {
    val context = LocalContext.current
    when (currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            PinDialog(trackName = listens[currentIndex!!].trackMetadata.trackName, artistName = listens[currentIndex].trackMetadata.artistName, onDismiss = deactivateDialog, onSubmit = {
                blurbContent ->
                onPin(Metadata(trackMetadata = listens[currentIndex].trackMetadata) , blurbContent)
            })
            LaunchedEffect(socialUiState.error){
                if(socialUiState.error == null){
                    snackbarState.showSnackbar(context.getString(R.string.pin_greeting))
                }
            }
        }
        Dialog.PERSONAL_RECOMMENDATION -> {
            PersonalRecommendationDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                onDismiss = deactivateDialog,
                searchResult = feedUiState.searchResult,
                searchUsers = searchUsers,
                onSubmit = {
                    users, blurbContent ->
                    onPersonallyRecommend(
                        Metadata(trackMetadata = listens[currentIndex].trackMetadata),
                        users,
                        blurbContent
                    )
                }
            )
        }
        Dialog.REVIEW -> {
            ReviewDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                artistName = listens[currentIndex].trackMetadata.artistName,
                releaseName = listens[currentIndex].trackMetadata.releaseName,
                onDismiss = deactivateDialog,
                isCritiqueBrainzLinked = isCritiqueBrainzLinked,
                onSubmit  = { type, blurbContent, rating, locale ->
                    onReview(
                        type,
                        blurbContent,
                        rating,
                        locale,
                        Metadata(trackMetadata = listens[currentIndex].trackMetadata)
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun ListensScreenPreview() {
    ListensScreen(
        onScrollToTop = {},
        scrollRequestState = false,
        updateNotificationServicePermissionStatus = {},
        uiState = ListensUiState(),
        feedUiState = FeedUiState(),
        preferencesUiState = PreferencesUiState(),
        validateUserToken = { true },
        setToken = {},
        playListen = {},
        socialUiState = SocialUiState(),
        onRecommend = {},
        onErrorShown = {},
        onMessageShown = {},
        onPin = {_,_ ->},
        searchUsers = {_ ->},
        isCritiqueBrainzLinked = { true },
        onReview = {_,_,_,_,_ ->},
        onPersonallyRecommend = {_,_,_ ->},
        dropdownItemIndex = remember { mutableStateOf(null) },
        snackbarState = remember { SnackbarHostState() }
    )
}
