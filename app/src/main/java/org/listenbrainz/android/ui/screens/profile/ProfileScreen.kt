package org.listenbrainz.android.ui.screens.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Utils.toSp
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    viewModel: UserViewModel = koinViewModel(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    username: String?,
    topBarActions: TopBarActions,
    snackbarState: SnackbarHostState,
    goToUserProfile: (String) -> Unit,
    goToArtistPage: (String) -> Unit,
    goToPlaylist: (String) -> Unit,
    navigateToCreateAccount: () -> Unit,
    socialViewModel: SocialViewModel = koinViewModel(),
) {
    val scrollState = rememberScrollState()
    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            scrollState.animateScrollTo(0)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val loginStatus by viewModel.loginStatusFlow.collectAsState()
    val loggedInUser = uiState.loggedInUser

    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Profile.title
        )

        AnimatedContent(
            targetState = loginStatus,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { status ->
            if (status == null) {
                LoadingAnimation()
            } else if (status == STATUS_LOGGED_IN && (!username.isNullOrEmpty() || !loggedInUser.isNullOrEmpty())) {
                BaseProfileScreen(
                    username = username ?: loggedInUser!!,
                    snackbarState = snackbarState,
                    onScrollToTop = onScrollToTop,
                    scrollRequestState = scrollRequestState,
                    goToUserProfile = goToUserProfile,
                    goToArtistPage = goToArtistPage,
                    goToPlaylist = goToPlaylist,
                    socialViewModel = socialViewModel,
                    deleted = socialViewModel.deletedListens
                )
            } else {
                LoginScreen(
                    navigateToCreateAccount = navigateToCreateAccount,
                    navigateToUserProfile = {
                        goToUserProfile(viewModel.appPreferences.username.get())
                    }
                )
            }
        }
    }
}