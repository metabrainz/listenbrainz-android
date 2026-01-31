package org.listenbrainz.android.ui.screens.main

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.navigation.AdaptiveNavigationBar
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.navigation.NavBarReorderOverlay
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
import org.listenbrainz.android.ui.screens.search.BrainzPlayerSearchScreen
import org.listenbrainz.android.ui.screens.search.UserSearchScreen
import org.listenbrainz.android.ui.screens.search.rememberSearchBarState
import org.listenbrainz.android.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.Utils.toPx
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.ListeningNowViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    dashBoardViewModel: DashBoardViewModel = koinViewModel(),
    brainzPlayerViewModel: BrainzPlayerViewModel = koinViewModel(),
    listeningNowViewModel: ListeningNowViewModel = koinViewModel(),
    settingsCallbacks: SettingsCallbacksToHomeScreen
) {
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val navController = rememberNavController()
    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
    var scrollToTopState by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val username by dashBoardViewModel.usernameFlow.collectAsStateWithLifecycle(
        initialValue = null
    )
    var showNavReorderOverlay by rememberSaveable { mutableStateOf(false) }
    val currentlyPlayingSong by brainzPlayerViewModel.currentlyPlayingSong.collectAsStateWithLifecycle()
    val currentPlayableState by brainzPlayerViewModel.currentPlayable.collectAsStateWithLifecycle()
    val isLandScape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isBackdropInitialised by remember {
        derivedStateOf {
            val currentOffset = runCatching {
                backdropScaffoldState.requireOffset()
            }.getOrNull()

            currentOffset != null
        }
    }
    val listeningNowUIState by listeningNowViewModel.listeningNowUIState.collectAsStateWithLifecycle()

    var maxOffset by remember {
        mutableFloatStateOf(0f)
    }

    val playerHeight = ListenBrainzTheme.sizes.brainzPlayerPeekHeight.toPx()
    LaunchedEffect(isBackdropInitialised) {
        if (isBackdropInitialised) {
            maxOffset =
                maxOf(maxOffset, backdropScaffoldState.requireOffset() - playerHeight)
        }
    }

    val desiredBackgroundColor by remember {
        derivedStateOf {
            brainzPlayerViewModel.playerBackGroundColor.copy(
                alpha = runCatching {
                    1 - (backdropScaffoldState.requireOffset() / maxOffset).coerceIn(
                        0f,
                        1f
                    )
                }.getOrElse { 0f }
            )
        }
    }

    val isNothingPlaying = remember(currentlyPlayingSong) {
        currentlyPlayingSong.toSong.title == "null"
                && currentlyPlayingSong.toSong.artist == "null"
                || currentPlayableState.songs.isEmpty()
    }

    val isListeningNowOpenedInConcealedState = backdropScaffoldState.targetValue != BackdropValue.Revealed && isNothingPlaying && listeningNowUIState.isListeningNow
    val isAudioPermissionGranted = permissions[PermissionEnum.ACCESS_MUSIC_AUDIO] == PermissionStatus.GRANTED || !PermissionEnum.ACCESS_MUSIC_AUDIO.isPermissionApplicable()

    val topBarActions = TopBarActions(
        popBackStackInSettingsScreen = {
            navController.popBackStack()
        },
        navigateToSettingsScreen = {
            navController.navigate(AppNavigationItem.Settings.route) {
                launchSingleTop = true
                restoreState = true
            }
        },
        activateSearch = {
            searchBarState.activate()
        }
    )
    val navOrder by dashBoardViewModel.navBarOrderFlow
        .collectAsStateWithLifecycle()

    val filteredNavItems = navOrder?.filter {
        isAudioPermissionGranted || it != AppNavigationItem.BrainzPlayer
    }
    val startRoute = filteredNavItems?.firstOrNull()?.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.background)
            .background(desiredBackgroundColor),
        bottomBar = {
            AnimatedVisibility(
                visible = !isListeningNowOpenedInConcealedState,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 400, easing = EaseInOut),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 400, easing = EaseInOut),
                    targetOffsetY = { it }
                )
            ) {
                if (!isLandScape) {
                    AdaptiveNavigationBar(
                        navController = navController,
                        items = filteredNavItems,
                        backdropScaffoldState = backdropScaffoldState,
                        scrollToTop = { scrollToTopState = true },
                        username = username,
                        isLandscape = false,
                        currentlyPlayingSong = currentlyPlayingSong.toSong,
                        songList = currentPlayableState.songs,
                        listeningNowUIState = listeningNowUIState,
                        searchBarState = searchBarState,
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.safeDrawingPadding(),
                hostState = snackbarState
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.inverseOnSurface,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.captionBar
    ) {
        Row {
            if (isLandScape) {
                AdaptiveNavigationBar(
                    navController = navController,
                    items = filteredNavItems,
                    backdropScaffoldState = backdropScaffoldState,
                    scrollToTop = { scrollToTopState = true },
                    username = username,
                    isLandscape = true,
                    currentlyPlayingSong = currentlyPlayingSong.toSong,
                    listeningNowUIState = listeningNowUIState,
                    songList = currentPlayableState.songs,
                    searchBarState = searchBarState
                )
            }
//            if (isGrantedPerms == PermissionStatus.GRANTED.name) {
            if (startRoute != null && filteredNavItems != null) {
                BrainzPlayerBackDropScreen(
                    modifier = Modifier.then(if (!isLandScape && !isListeningNowOpenedInConcealedState) Modifier.navigationBarsPadding() else Modifier),
                    backdropScaffoldState = backdropScaffoldState,
                    paddingValues = it,
                    brainzPlayerViewModel = brainzPlayerViewModel,
                    isLandscape = isLandScape,
                    listeningNowViewModel = listeningNowViewModel
                ) {
                    AppNavigation(
                        navController = navController,
                        scrollRequestState = scrollToTopState,
                        onScrollToTop = { scrollToTop ->
                            scope.launch {
                                if (scrollToTopState) {
                                    scrollToTop()
                                    scrollToTopState = false
                                }
                            }
                        },
                        snackbarState = snackbarState,
                        dashBoardViewModel = dashBoardViewModel,
                        topAppBarActions = topBarActions,
                        settingsCallbacks = settingsCallbacks,
                        startRoute = startRoute,
                        onNavigationReorderClick = { showNavReorderOverlay = true }
                    )
                }
            }
        }

        when (currentDestination?.route) {
            AppNavigationItem.BrainzPlayer.route -> BrainzPlayerSearchScreen(
                isActive = searchBarState.isActive,
                deactivate = searchBarState::deactivate,
            )

            else -> UserSearchScreen(
                isActive = searchBarState.isActive,
                deactivate = searchBarState::deactivate,
                goToUserPage = { username ->
                    searchBarState.deactivate()
                    navController.navigate(AppNavigationItem.Profile.withUserArg(username))
                }
            )
        }
    }
    if (showNavReorderOverlay && navOrder!=null) {
        navOrder?.let { items ->
            NavBarReorderOverlay(
                items = items,
                isLandscape = isLandScape,
                onDismiss = { newOrder ->
                    scope.launch {
                        dashBoardViewModel.appPreferences.navBarOrder.set(newOrder)
                        showNavReorderOverlay = false
                    }
                }
            )
        }
    }
}
