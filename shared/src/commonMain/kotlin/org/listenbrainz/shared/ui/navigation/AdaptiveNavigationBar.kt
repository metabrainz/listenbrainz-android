package org.listenbrainz.shared.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.listenbrainz.shared.ui.screens.listeningNow.ListeningNowCard
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.BottomNavDefaults
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.viewmodel.ListeningNowUIState

@Composable
fun AdaptiveNavigationBar(
    modifier: Modifier = Modifier,
    items: List<AppNavigationItem>?,
    navigator: HomeNavigator,
    backgroundColor: Color = ListenBrainzTheme.colorScheme.nav,
    contentColor: Color = LocalContentColor.current,
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed),
    scrollToTop: () -> Unit,
    isLandscape: Boolean,
    listeningNowUIState: ListeningNowUIState,
) {
    val coroutineScope = rememberCoroutineScope()

    //composable with common navigation logic
    fun onItemClick(item: AppNavigationItem) {
        coroutineScope.launch {
            // Hide front layer
            backdropScaffoldState.reveal()
        }
        if (navigator.selectTab(item)) {
            scrollToTop()
        }
    }

    if (isLandscape) {
        NavigationRail(
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            items?.forEach { item ->
                AppNavigationRailItem(
                    item = item,
                    selected = navigator.currentTab == item,
                    onClick = { onItemClick(item) },
                    indicatorColor = backgroundColor,
                    contentColor = contentColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (listeningNowUIState.isListeningNow) {
                ListeningNowCard(
                    uiState = listeningNowUIState,
                    backdropScaffoldState = backdropScaffoldState,
                    isLandscape = true,
                    coroutineScope = coroutineScope,
                )
            }
        }
    } else {
        NavigationBar(
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            items?.forEach { item ->
                AppNavigationBarItem(
                    item = item,
                    selected = navigator.currentTab == item,
                    onClick = { onItemClick(item) },
                    indicatorColor = backgroundColor,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Preview(showSystemUi = true, device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun AdaptiveNavigationBarPreview() {
    AdaptiveNavigationBar(
        navigator = rememberHomeNavigator(AppNavigationItem.Feed),
        scrollToTop = {},
        isLandscape = true,
        listeningNowUIState = ListeningNowUIState(),
        items = BottomNavDefaults.items()
    )
}