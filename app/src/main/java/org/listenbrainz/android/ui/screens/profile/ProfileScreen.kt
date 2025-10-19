package org.listenbrainz.android.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    viewModel: UserViewModel = hiltViewModel(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    username: String?,
    topBarActions: TopBarActions,
    snackbarState: SnackbarHostState,
    goToUserProfile: (String) -> Unit,
    goToArtistPage: (String) -> Unit,
    goToPlaylist: (String) -> Unit,
    navigateToCreateAccount: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState.collectAsState()
    // Scroll to the top when shouldScrollToTop becomes true
    LaunchedEffect(scrollRequestState) {
        onScrollToTop {
            scrollState.animateScrollTo(0)
        }
    }

    val loginStatus by viewModel.loginStatusFlow.collectAsState()

    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Profile.title
        )
        when (loginStatus) {
            STATUS_LOGGED_IN -> {
                LaunchedEffect(Unit) {
                    viewModel.getUserDataFromRemote(username)
                }

                BaseProfileScreen(
                    username = username,
                    snackbarState = snackbarState,
                    uiState = uiState.value,
                    goToUserProfile = goToUserProfile,
                    goToArtistPage = goToArtistPage,
                    goToPlaylist = goToPlaylist,
                )
            }

            else -> LoginScreen(
                navigateToCreateAccount = navigateToCreateAccount,
                navigateToUserProfile =
             {
                goToUserProfile(viewModel.appPreferences.username.get())
            })
        }
    }
}