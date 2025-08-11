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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BackdropValue
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.navigation.AdaptiveNavigationBar
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.screens.search.BrainzPlayerSearchScreen
import org.listenbrainz.android.ui.screens.search.UserSearchScreen
import org.listenbrainz.android.ui.screens.search.rememberSearchBarState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.Utils.toPx
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
import org.listenbrainz.android.viewmodel.ListeningNowViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    dashBoardViewModel: DashBoardViewModel = hiltViewModel(),
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    listeningNowViewModel: ListeningNowViewModel = hiltViewModel()
){
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val navController = rememberNavController()
    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
    var scrollToTopState by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }
    val searchBarState = rememberSearchBarState()
    val brainzplayerSearchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val username by dashBoardViewModel.usernameFlow.collectAsStateWithLifecycle(
        initialValue = null
    )
    val currentlyPlayingSong by brainzPlayerViewModel.currentlyPlayingSong.collectAsStateWithLifecycle()
    val songList = brainzPlayerViewModel.appPreferences.currentPlayable?.songs
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
            println(maxOffset)
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
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.background)
            .background(desiredBackgroundColor),
        topBar = {
            AnimatedVisibility(
                visible = !listeningNowUIState.isListeningNow || !backdropScaffoldState.isConcealed,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300, easing = EaseInOut)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 100, easing = EaseInOut)
                )
            ) {
                TopBar(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = if (isLandScape) dimensionResource(R.dimen.navigation_rail_width) else 0.dp),
                    navController = navController,
                    searchBarState = when (currentDestination?.route) {
                        AppNavigationItem.BrainzPlayer.route -> brainzplayerSearchBarState
                        else -> searchBarState
                    },
                )
            }
        },
        bottomBar = {
            if (!isLandScape)
                AdaptiveNavigationBar(
                    navController = navController,
                    backdropScaffoldState = backdropScaffoldState,
                    scrollToTop = { scrollToTopState = true },
                    username = username,
                    isLandscape = false,
                    currentlyPlayingSong = currentlyPlayingSong.toSong,
                    backgroundColor = listeningNowUIState.palette?.gradientColors?.getOrNull(0)?.takeIf {
                        backdropScaffoldState.isConcealed
                    } ?: ListenBrainzTheme.colorScheme.nav,
                    contentColor = listeningNowUIState.palette?.titleTextColorDark?.takeIf {
                        backdropScaffoldState.isConcealed
                    },
                    songList = songList ?: emptyList(),
                    isAudioPermissionGranted = permissions[PermissionEnum.ACCESS_MUSIC_AUDIO] == PermissionStatus.GRANTED || !PermissionEnum.ACCESS_MUSIC_AUDIO.isPermissionApplicable(),
                    listeningNowUIState = listeningNowUIState
                )
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
                    backdropScaffoldState = backdropScaffoldState,
                    scrollToTop = { scrollToTopState = true },
                    username = username,
                    isLandscape = true,
                    currentlyPlayingSong = currentlyPlayingSong.toSong,
                    songList = songList ?: emptyList(),
                    listeningNowUIState = listeningNowUIState,
                    isAudioPermissionGranted = permissions[PermissionEnum.ACCESS_MUSIC_AUDIO] == PermissionStatus.GRANTED || !PermissionEnum.ACCESS_MUSIC_AUDIO.isPermissionApplicable()
                )
            }
//            if (isGrantedPerms == PermissionStatus.GRANTED.name) {
                BrainzPlayerBackDropScreen(
                    modifier = Modifier.then(if (!isLandScape) Modifier.navigationBarsPadding() else Modifier),
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
                        dashBoardViewModel = dashBoardViewModel
                    )
                }
//            }
        }

        when (currentDestination?.route) {
            AppNavigationItem.BrainzPlayer.route -> BrainzPlayerSearchScreen(
                isActive = brainzplayerSearchBarState.isActive,
                deactivate = brainzplayerSearchBarState::deactivate,
            )

            else -> UserSearchScreen(
                isActive = searchBarState.isActive,
                deactivate = searchBarState::deactivate,
                goToUserPage = { username ->
                    searchBarState.deactivate()
                    navController.navigate("${AppNavigationItem.Profile.route}/$username")
                }
            )
        }
    }

}