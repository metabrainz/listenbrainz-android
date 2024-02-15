package org.listenbrainz.android.ui.screens.listens

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.LoadingAnimation
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
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun ListensScreen(
    viewModel: ListensViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
) {
    
    val uiState by viewModel.uiState.collectAsState()
    val preferencesUiState by viewModel.preferencesUiState.collectAsState()
    
    ListensScreen(
        scrollRequestState = scrollRequestState,
        onScrollToTop = onScrollToTop,
        uiState = uiState,
        preferencesUiState = preferencesUiState,
        updateNotificationServicePermissionStatus = {
            viewModel.updateNotificationServicePermissionStatus()
        },
        validateUserToken = { token ->
            viewModel.validateUserToken(token)
        },
        setToken = {
            viewModel.setAccessToken(it)
        },
        playListen = {
            socialViewModel.playListen(it)
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
    socialViewModel: SocialViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    uiState: ListensUiState,
    preferencesUiState: PreferencesUiState,
    updateNotificationServicePermissionStatus: () -> Unit,
    validateUserToken: suspend (String) -> Boolean,
    setToken: (String) -> Unit,
    playListen: (TrackMetadata) -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current
) {
    val listState = rememberLazyListState()
    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    val dialogsState = rememberDialogsState()
    val feedUiState by feedViewModel.uiState.collectAsState()
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
                            onRecommend = {
                                try {
                                    socialViewModel.recommend(metadata)
                                    Toast.makeText(context , Constants.Strings.RECOMMENDATION_GREETING , Toast.LENGTH_SHORT).show()
                                }
                                catch (e : Error) {
                                    Toast.makeText(context , Constants.Strings.ERROR_MESSAGE , Toast.LENGTH_SHORT).show()
                                }
                                dropdownItemIndex.value = null
                            },
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
                                    Toast.makeText(context , Constants.Strings.ERROR_MESSAGE , Toast.LENGTH_SHORT).show()
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
        Dialogs(
            deactivateDialog = {
                dialogsState.deactivateDialog()
            },
            currentDialog = dialogsState.currentDialog,
            currentIndex = dialogsState.metadata?.getInt(ListenDialogBundleKeys.EVENT_INDEX.name),
            listens = uiState.listens,
            onPin = {metadata, blurbContent ->  socialViewModel.pin(metadata , blurbContent)},
            searchUsers = {
                query -> feedViewModel.searchUser(query)
            },
            feedUiState = feedUiState,
            isCritiqueBrainzLinked = { feedViewModel.isCritiqueBrainzLinked() },
            onReview = {type, blurbContent, rating, locale, metadata -> socialViewModel.review(metadata , type , blurbContent , rating , locale) },
            onPersonallyRecommend = {metadata, users, blurbContent -> socialViewModel.personallyRecommend(metadata, users, blurbContent)}
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
    onPersonallyRecommend : (metadata : Metadata , users : List<String> , blurbContent : String) -> Unit

) {
    val context = LocalContext.current
    when (currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            PinDialog(trackName = listens[currentIndex!!].trackMetadata.trackName, artistName = listens[currentIndex].trackMetadata.artistName, onDismiss = deactivateDialog, onSubmit = {
                blurbContent -> try {
                onPin(Metadata(trackMetadata = listens[currentIndex].trackMetadata) , blurbContent)
                Toast.makeText(context , Constants.Strings.PIN_GREETING , Toast.LENGTH_SHORT).show()
                } catch (e : Error) {
                Toast.makeText(context , Constants.Strings.ERROR_MESSAGE , Toast.LENGTH_SHORT).show()
                }
            })
        }
        Dialog.PERSONAL_RECOMMENDATION -> {
            PersonalRecommendationDialog(
                trackName = listens[currentIndex!!].trackMetadata.trackName,
                onDismiss = deactivateDialog,
                searchResult = feedUiState.searchResult,
                searchUsers = searchUsers,
                onSubmit = {
                    users, blurbContent -> try {
                    onPersonallyRecommend(
                        Metadata(trackMetadata = listens[currentIndex].trackMetadata),
                        users,
                        blurbContent
                    )
                    Toast.makeText(context , Constants.Strings.PERSONAL_RECOMMENDATION_GREETING , Toast.LENGTH_SHORT).show()
                }
                catch (e : Error) {
                    Toast.makeText(context , Constants.Strings.ERROR_MESSAGE , Toast.LENGTH_SHORT).show()
                }
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
                onSubmit  = {
                    type, blurbContent, rating, locale  -> try {
                    onReview(type, blurbContent, rating, locale , Metadata(trackMetadata = listens[currentIndex].trackMetadata))
                    Toast.makeText(context , Constants.Strings.REVIEW_GREETING , Toast.LENGTH_SHORT).show()
                    }
                catch (e : Error) {
                    Toast.makeText(context , Constants.Strings.ERROR_MESSAGE , Toast.LENGTH_SHORT).show()
                }
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
        preferencesUiState = PreferencesUiState(),
        validateUserToken = { true },
        setToken = {},
        playListen = {}
    )
}
