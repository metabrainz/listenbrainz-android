package org.listenbrainz.android.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.listens.ListensScreen
import org.listenbrainz.android.ui.screens.profile.stats.StatsScreen
import org.listenbrainz.android.ui.screens.profile.taste.TasteScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun BaseProfileScreen(
    username: String?,
    snackbarState: SnackbarHostState,
    viewModel: UserViewModel = hiltViewModel(),
    uiState: ProfileUiState,
    onFollowClick: (String) -> Unit,
    onUnfollowClick: (String) -> Unit,
    goToUserProfile: () -> Unit,
    feedViewModel: FeedViewModel = hiltViewModel(),
    listensViewModel: ListensViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String?) -> Unit,
) {
    val pagerState = rememberPagerState { ProfileScreenTab.entries.size }
    val isLoggedInUser = uiState.isSelf
    val uriHandler = LocalUriHandler.current
    var mbOpeningErrorState by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(mbOpeningErrorState) {
        if (mbOpeningErrorState != null) {
            snackbarState.showSnackbar("Some Error Occurred", duration = SnackbarDuration.Short)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.listensTabUiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
        AnimatedVisibility(visible = !uiState.listensTabUiState.isLoading) {
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

                    repeat(ProfileScreenTab.entries.size + 1) { position ->
                        when (position) {
                            0 -> Box(
                                modifier = Modifier
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
                                                    goToUserProfile()
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

                            else -> {
                                val adjustedPosition = position - 1
                                ElevatedSuggestionChip(
                                    modifier = Modifier.padding(ListenBrainzTheme.paddings.chipsHorizontal),
                                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                        if (adjustedPosition == pagerState.currentPage) {
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
                                            text = ProfileScreenTab.entries.firstOrNull { it.index == adjustedPosition }?.value ?: "",
                                            style = ListenBrainzTheme.textStyles.chips,
                                            color = ListenBrainzTheme.colorScheme.text
                                        )
                                    },
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(adjustedPosition)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 20.dp, bottom = 4.dp)
                ) {
                    if (pagerState.currentPage == ProfileScreenTab.LISTENS.index) {
                        when (isLoggedInUser) {
                            true -> AddListensButton()
                            false ->
                                FollowButton(
                                    modifier = Modifier,
                                    isFollowedState = uiState.listensTabUiState.isFollowing,
                                    onClick = {
                                        if (uiState.listensTabUiState.isFollowing) {
                                            onUnfollowClick(username ?: "")
                                        } else {
                                            onFollowClick(username ?: "")
                                        }
                                    }
                                )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        MusicBrainzButton {
                            try {
                                uriHandler.openUri(Constants.MB_BASE_URL + "user/${username}")
                            } catch (e: RuntimeException) {
                                mbOpeningErrorState = e.message
                            } catch (e: Exception) {
                                mbOpeningErrorState = e.message
                            }
                        }
                    }
                }


                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    key = { ProfileScreenTab.entries[it] }
                ) { index ->
                    when (index) {
                        ProfileScreenTab.LISTENS.index -> ListensScreen(
                            scrollRequestState = false,
                            userViewModel = viewModel,
                            onScrollToTop = {},
                            snackbarState = snackbarState,
                            username = username,
                            socialViewModel = socialViewModel,
                            viewModel = listensViewModel,
                            goToArtistPage = goToArtistPage,
                            goToUserPage = goToUserPage,
                        )

                        ProfileScreenTab.STATS.index -> StatsScreen(
                            username = username,
                            snackbarState = snackbarState,
                            socialViewModel = socialViewModel,
                            viewModel = viewModel,
                            feedViewModel = feedViewModel,
                            goToArtistPage = goToArtistPage
                        )

                        ProfileScreenTab.TASTE.index -> TasteScreen(
                            snackbarState = snackbarState,
                            socialViewModel = socialViewModel,
                            feedViewModel = feedViewModel,
                            viewModel = viewModel,
                            goToArtistPage = goToArtistPage
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AddListensButton() {
    IconButton(
        onClick = { /*TODO*/ }, modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF353070))
            .width(110.dp)
            .height(30.dp)
    ) {
        Row(modifier = Modifier.padding(all = 4.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "",
                tint = new_app_bg_light,
                modifier = Modifier
                    .width(10.dp)
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "Add Listens",
                color = new_app_bg_light,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MusicBrainzButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick, modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF353070))
            .width(140.dp)
            .height(30.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.musicbrainz_logo),
                contentDescription = "",
                modifier = Modifier
                    .width(20.dp)
                    .height(30.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "MusicBrainz",
                color = new_app_bg_light,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "",
                tint = new_app_bg_light,
                modifier = Modifier
                    .width(30.dp)
                    .height(20.dp)
            )
        }
    }
}
