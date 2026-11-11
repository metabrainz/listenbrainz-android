package org.listenbrainz.shared.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.listenbrainz.shared.ui.screens.album.AlbumScreen
import org.listenbrainz.shared.ui.screens.artist.ArtistScreen
import org.listenbrainz.shared.ui.screens.explore.ExploreScreen
import org.listenbrainz.shared.ui.screens.feed.FeedScreen
import org.listenbrainz.shared.ui.screens.playlist.PlaylistDetailScreen
import org.listenbrainz.shared.ui.screens.profile.ProfileScreen
import org.listenbrainz.shared.ui.screens.search.BaseSearchScreen
import org.listenbrainz.shared.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.shared.ui.screens.settings.SettingsScreen
import org.listenbrainz.shared.navigation.NavigationItem
import org.listenbrainz.shared.ui.fadeOnTopTransition
import org.listenbrainz.shared.viewmodel.DashBoardViewModel

@Composable
fun AppNavigation(
    navigator: HomeNavigator,
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    dashBoardViewModel: DashBoardViewModel,
    snackbarState: SnackbarHostState,
    settingsCallbacks: SettingsCallbacksToHomeScreen,
    onNewsClick: () -> Unit,
    topAppBarActions: TopBarActions,
    onNavigationReorderClick: () -> Unit,
    onDonateClick: () -> Unit,
    listenSubmissionSettings: @Composable () -> Unit
) {
    val dashboardUiState by dashBoardViewModel.uiState.collectAsStateWithLifecycle()

    fun goToUserProfile(username: String) {
        navigator.navigate(NavigationItem.ProfileScreen(username))
    }

    fun goToArtistPage(mbid: String) {
        navigator.navigate(NavigationItem.ArtistScreen(mbid))
    }

    fun goToAlbumPage(mbid: String) {
        navigator.navigate(NavigationItem.AlbumScreen(mbid))
    }

    fun goToPlaylist(mbid: String) {
        navigator.navigate(NavigationItem.PlaylistScreen(mbid))
    }

    val entryProvider = entryProvider<NavKey> {
        entry<NavigationItem.FeedScreen> {
            FeedScreen(
                scrollToTopState = scrollRequestState,
                onScrollToTop = onScrollToTop,
                goToUserPage = ::goToUserProfile,
                goToArtistPage = ::goToArtistPage,
                topAppBarActions = topAppBarActions
            )
        }
        entry<NavigationItem.ExploreScreen> {
            ExploreScreen(
                topAppBarActions,
                username = dashboardUiState.username,
                onNewsClick = onNewsClick,
            )
        }
        entry<NavigationItem.ProfileScreen> { key ->
            ProfileScreen(
                onScrollToTop = onScrollToTop,
                scrollRequestState = scrollRequestState,
                username = key.username ?: dashboardUiState.username,
                snackbarState = snackbarState,
                goToUserProfile = ::goToUserProfile,
                goToArtistPage = ::goToArtistPage,
                goToPlaylist = ::goToPlaylist,
                topBarActions = topAppBarActions,
            )
        }
        entry<NavigationItem.SettingsScreen>(metadata = fadeOnTopTransition) {
            SettingsScreen(
                callbacks = settingsCallbacks,
                topBarActions = topAppBarActions,
                onNavigationReorderClick = onNavigationReorderClick,
                onDonateClick = onDonateClick,
                listenSubmissionSettings = listenSubmissionSettings
            )
        }
        entry<NavigationItem.ArtistScreen> { key ->
            ArtistScreen(
                artistMbid = key.mbid,
                goToArtistPage = ::goToArtistPage,
                snackBarState = snackbarState,
                goToUserPage = ::goToUserProfile,
                goToAlbumPage = ::goToAlbumPage,
                topBarActions = topAppBarActions
            )
        }
        entry<NavigationItem.AlbumScreen> { key ->
            AlbumScreen(
                albumMbid = key.mbid,
                snackBarState = snackbarState,
                topBarActions = topAppBarActions
            )
        }
        entry<NavigationItem.PlaylistScreen> { key ->
            PlaylistDetailScreen(
                playlistMBID = key.mbid,
                snackbarState = snackbarState,
                goToArtistPage = ::goToArtistPage,
                goToUserPage = ::goToUserProfile,
                topBarActions = topAppBarActions
            )
        }
        entry<NavigationItem.SearchScreen> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                BaseSearchScreen(
                    deactivate = navigator::pop,
                    goToUserPage = ::goToUserProfile,
                    goToPlaylist = ::goToPlaylist,
                    goToArtist = ::goToArtistPage,
                    goToAlbum = ::goToAlbumPage
                )
            }
        }
    }

    val parentViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    // Decorated for every tab, not just the visible ones, so hidden tabs keep their state
    val tabEntries = navigator.tabStacks.mapValues { (tab, backStack) ->
        // Not rememberViewModelStoreProvider, which clears every store when Home is covered
        val viewModelStoreProvider = remember(parentViewModelStoreOwner) {
            ViewModelStoreProvider(parentViewModelStoreOwner, "home:${tab.route}")
        }
        rememberDecoratedNavEntries(
            entries = backStack.mapIndexed { index, key ->
                val entry = entryProvider(key)
                // The same page can be open more than once, in one tab or several
                NavEntry(key, "${tab.route}:$index:${entry.contentKey}", entry.metadata) {
                    entry.Content()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(viewModelStoreProvider)
            )
        )
    }

    NavDisplay(
        entries = navigator.visibleTabs.flatMap { tabEntries.getValue(it) },
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { fadeTransition() },
        popTransitionSpec = { fadeTransition() },
        predictivePopTransitionSpec = { fadeTransition() },
        onBack = navigator::pop
    )
}

private fun fadeTransition(): ContentTransform =
    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
