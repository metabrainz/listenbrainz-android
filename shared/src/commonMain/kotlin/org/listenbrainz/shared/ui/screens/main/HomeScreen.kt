package org.listenbrainz.shared.ui.screens.main

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.ui.navigation.AdaptiveNavigationBar
import org.listenbrainz.shared.ui.navigation.AppNavigation
import org.listenbrainz.shared.ui.navigation.NavBarReorderOverlay
import org.listenbrainz.shared.ui.navigation.TopBarActions
import org.listenbrainz.shared.ui.navigation.rememberHomeNavigator
import org.listenbrainz.shared.ui.screens.listeningNow.ListeningNowBackDropScreen
import org.listenbrainz.shared.ui.screens.settings.SettingsCallbacksToHomeScreen
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.viewmodel.DashBoardViewModel
import org.listenbrainz.shared.viewmodel.ListeningNowViewModel
import org.listenbrainz.shared.navigation.NavigationItem


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    dashBoardViewModel: DashBoardViewModel = koinViewModel(),
    listeningNowViewModel: ListeningNowViewModel = koinViewModel(),
    settingsCallbacks: SettingsCallbacksToHomeScreen,
    onNewsClick: () -> Unit,
    onDonateClick: () -> Unit,
    listenSubmissionSettings: @Composable () -> Unit = {}
) {
    val dashboardUiState by dashBoardViewModel.uiState.collectAsStateWithLifecycle()
    val backdropScaffoldState =
        rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
    var scrollToTopState by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showNavReorderOverlay by rememberSaveable { mutableStateOf(false) }
    val isLandScape = LocalWindowInfo.current.containerSize.let { it.width > it.height }
    val listeningNowUIState by listeningNowViewModel.listeningNowUIState.collectAsStateWithLifecycle()

    val isListeningNowOpenedInConcealedState = backdropScaffoldState.targetValue != BackdropValue.Revealed && listeningNowUIState.isListeningNow

    val navOrder = dashboardUiState.navBarOrder
    val navigator = rememberHomeNavigator(startTab = navOrder?.firstOrNull() ?: AppNavigationItem.Feed)

    val topBarActions = TopBarActions(
        popBackStackInSettingsScreen = navigator::pop,
        navigateToSettingsScreen = {
            navigator.navigate(NavigationItem.SettingsScreen)
        },
        activateSearch = {
            navigator.navigate(NavigationItem.SearchScreen)
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.background),
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
                        navigator = navigator,
                        items = navOrder,
                        backdropScaffoldState = backdropScaffoldState,
                        scrollToTop = { scrollToTopState = true },
                        isLandscape = false,
                        listeningNowUIState = listeningNowUIState,
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
                    navigator = navigator,
                    items = navOrder,
                    backdropScaffoldState = backdropScaffoldState,
                    scrollToTop = { scrollToTopState = true },
                    isLandscape = true,
                    listeningNowUIState = listeningNowUIState,
                )
            }
//            if (isGrantedPerms == PermissionStatus.GRANTED.name) {
            if (navOrder != null) {
                ListeningNowBackDropScreen(
                    modifier = Modifier.then(if (!isLandScape && !isListeningNowOpenedInConcealedState) Modifier.navigationBarsPadding() else Modifier),
                    backdropScaffoldState = backdropScaffoldState,
                    paddingValues = it,
                    isLandscape = isLandScape,
                    listeningNowViewModel = listeningNowViewModel
                ) {
                    AppNavigation(
                        navigator = navigator,
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
                        onNewsClick = onNewsClick,
                        onNavigationReorderClick = { showNavReorderOverlay = true },
                        onDonateClick = onDonateClick,
                        listenSubmissionSettings = listenSubmissionSettings
                    )
                }
            }
        }

    }
    if (showNavReorderOverlay && navOrder != null) {
        NavBarReorderOverlay(
            items = navOrder,
            isLandscape = isLandScape,
            onDismiss = { newOrder ->
                navigator.pinCurrentTab()
                scope.launch {
                    dashBoardViewModel.appPreferences.navBarOrder.set(newOrder)
                    showNavReorderOverlay = false
                }
            }
        )
    }
}
