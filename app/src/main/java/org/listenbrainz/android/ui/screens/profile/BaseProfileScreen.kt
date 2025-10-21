package org.listenbrainz.android.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.createdforyou.CreatedForYouScreen
import org.listenbrainz.android.ui.screens.profile.listens.ListensScreen
import org.listenbrainz.android.ui.screens.profile.playlists.UserPlaylistScreen
import org.listenbrainz.android.ui.screens.profile.stats.StatsScreen
import org.listenbrainz.android.ui.screens.profile.taste.TasteScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Utils.LaunchedEffectUnit
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun BaseProfileScreen(
    username: String?,
    snackbarState: SnackbarHostState,
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
    goToUserProfile: (String) -> Unit,
    feedViewModel: FeedViewModel = hiltViewModel(),
    listensViewModel: ListensViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    playlistDataViewModel: PlaylistDataViewModel = hiltViewModel(),
    goToPlaylist: (String) -> Unit,
    goToArtistPage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState { ProfileScreenTab.entries.size }
    val currentTab by remember {
        derivedStateOf {
            ProfileScreenTab.entries.first { it.index == pagerState.currentPage }
        }
    }

    fun ProfileScreenTab.isLoading(): Boolean {
        return when (this) {
            ProfileScreenTab.LISTENS -> uiState.listensTabUiState.isLoading
            ProfileScreenTab.STATS -> uiState.statsTabUIState.isLoading
            ProfileScreenTab.TASTE -> uiState.tasteTabUIState.isLoading
            ProfileScreenTab.PLAYLISTS -> uiState.playlistTabUIState.isLoading
            ProfileScreenTab.CREATED_FOR_YOU -> uiState.createdForTabUIState.isLoading
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(username) {
        if (!username.isNullOrEmpty())
            viewModel.updateUser(username)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ListenBrainzTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal / 2))

            val nameChipBringIntoViewRequester = remember { BringIntoViewRequester() }

            Box(
                modifier = Modifier
                    .bringIntoViewRequester(nameChipBringIntoViewRequester)
                    .padding(ListenBrainzTheme.paddings.chipsHorizontal)
                    .clip(shape = RoundedCornerShape(4.dp))
                    .background(
                        when (uiState.isSelf) {
                            true -> lb_purple
                            false -> lb_orange
                        }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(
                        end = 8.dp, top = when (uiState.isSelf) {
                            true -> 4.dp
                            false -> 0.dp
                        }, bottom = when (uiState.isSelf) {
                            true -> 4.dp
                            false -> 0.dp
                        }
                    ), verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!uiState.isSelf) {
                        Box(
                            modifier = Modifier
                                .background(lb_purple)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "",
                                tint = new_app_bg_light,
                                modifier = Modifier.clickable {
                                    if (username != null) {
                                        goToUserProfile(username)
                                    }
                                })
                        }
                    }
                    Text(
                        username ?: "", color = when (uiState.isSelf) {
                            true -> Color.White
                            false -> Color.Black
                        }, modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            repeat(ProfileScreenTab.entries.size) { position ->
                val bringIntoViewRequester = remember { BringIntoViewRequester() }

                LaunchedEffect(pagerState.currentPage) {
                    if (position == pagerState.currentPage) {
                        if (position == 0) {
                            nameChipBringIntoViewRequester.bringIntoView()
                        } else {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }

                ElevatedSuggestionChip(
                    modifier = Modifier
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .padding(ListenBrainzTheme.paddings.chipsHorizontal),
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (position == pagerState.currentPage) {
                            ListenBrainzTheme.colorScheme.chipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.chipUnselected
                        }
                    ),
                    shape = ListenBrainzTheme.shapes.chips,
                    elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(
                        elevation = 4.dp
                    ),
                    label = {
                        Text(
                            text = ProfileScreenTab.entries.firstOrNull { it.index == position }?.value
                                ?: "",
                            style = ListenBrainzTheme.textStyles.chips,
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    },
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(position)
                        }
                    }
                )
            }
        }

        LaunchedEffect(currentTab) {
            when (currentTab) {
                ProfileScreenTab.LISTENS -> viewModel.getUserListensData()
                ProfileScreenTab.STATS -> viewModel.getUserStatsData()
                ProfileScreenTab.TASTE -> viewModel.getUserTasteData()
                ProfileScreenTab.CREATED_FOR_YOU -> viewModel.getCreatedForYouPlaylists()
                else -> Unit
            }
        }

        @Composable
        fun WithLoader(tab: ProfileScreenTab, content: @Composable () -> Unit) {
            AnimatedContent(
                targetState = tab.isLoading(),
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { isLoading ->
                if (isLoading) {
                    LoadingAnimation()
                } else {
                    content()
                }
            }
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top,
            key = { ProfileScreenTab.entries[it] }
        ) { index ->
            when (index) {
                ProfileScreenTab.LISTENS.index -> WithLoader(ProfileScreenTab.LISTENS) {
                    ListensScreen(
                        scrollRequestState = false,
                        userViewModel = viewModel,
                        onScrollToTop = {},
                        snackbarState = snackbarState,
                        username = username,
                        socialViewModel = socialViewModel,
                        viewModel = listensViewModel,
                        goToArtistPage = goToArtistPage,
                        goToUserProfile = goToUserProfile
                    )
                }

                ProfileScreenTab.STATS.index -> WithLoader(ProfileScreenTab.STATS) {
                    StatsScreen(
                        username = username,
                        snackbarState = snackbarState,
                        socialViewModel = socialViewModel,
                        viewModel = viewModel,
                        feedViewModel = feedViewModel,
                        goToArtistPage = goToArtistPage
                    )
                }

                ProfileScreenTab.TASTE.index -> WithLoader(ProfileScreenTab.TASTE) {
                    TasteScreen(
                        snackbarState = snackbarState,
                        socialViewModel = socialViewModel,
                        feedViewModel = feedViewModel,
                        viewModel = viewModel,
                        goToArtistPage = goToArtistPage
                    )
                }

                ProfileScreenTab.PLAYLISTS.index -> WithLoader(ProfileScreenTab.PLAYLISTS) {
                    UserPlaylistScreen(
                        snackbarState = snackbarState,
                        userViewModel = viewModel,
                        playlistViewModel = playlistDataViewModel,
                        goToPlaylist
                    )
                }

                ProfileScreenTab.CREATED_FOR_YOU.index -> WithLoader(ProfileScreenTab.CREATED_FOR_YOU) {
                    CreatedForYouScreen(
                        snackbarState = snackbarState,
                        userViewModel = viewModel,
                        goToArtistPage = goToArtistPage,
                        socialViewModel = socialViewModel
                    )
                }
            }
        }
    }
}
