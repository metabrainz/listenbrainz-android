package org.listenbrainz.android.ui.navigation

import ReorderableBottomNavBar
import ReorderableNavRail
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.NavigationRail
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun NavBarReorderOverlay(
    items: List<AppNavigationItem>,
    isLandscape: Boolean,
    onDismiss: (List<AppNavigationItem>) -> Unit
) {
    val mutableItems = rememberSaveable(
        saver = listSaver(
            save = { list -> list.map { it.route } },
            restore = { routes ->
                mutableStateListOf<AppNavigationItem>().apply {
                    addAll(
                        routes.mapNotNull { route ->
                            BottomNavDefaults.items()
                                .firstOrNull { it.route == route }
                        }
                    )
                }
            }
        )
    ) {
        mutableStateListOf<AppNavigationItem>().apply {
            addAll(items)
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        mutableItems.apply {
            add(to.index, removeAt(from.index))
        }
    }

    BackHandler(true) {
        onDismiss(mutableItems.toList())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .pointerInput(Unit) {}
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val color = Color.White
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(3.dp, color, CircleShape)
                    .clickable {
                        onDismiss(mutableItems.toList())
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.nav_reorder_hint),
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DummyAdaptiveNavBar(
            items = mutableItems,
            lazyListState = lazyListState,
            reorderableLazyListState = reorderableLazyListState,
            isLandscape = isLandscape,
            modifier = if (isLandscape) {
                Modifier.align(Alignment.CenterStart)
            } else {
                Modifier.align(Alignment.BottomCenter)
            }
        )
    }
}

@Composable
private fun DummyAdaptiveNavBar(
    items: SnapshotStateList<AppNavigationItem>,
    lazyListState: LazyListState,
    reorderableLazyListState: ReorderableLazyListState,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = ListenBrainzTheme.colorScheme.nav

    if (isLandscape) {
        NavigationRail(
            modifier = modifier
                .widthIn(max = dimensionResource(R.dimen.navigation_rail_width))
                .background(backgroundColor)
                .statusBarsPadding(),
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            elevation = 0.dp
        ) {
            ReorderableNavRail(items, lazyListState, reorderableLazyListState, backgroundColor)
        }
    } else {
        BottomNavigation(
            modifier = modifier,
            backgroundColor = backgroundColor,
            elevation = 0.dp
        ) {
            ReorderableBottomNavBar(items, lazyListState, reorderableLazyListState, this)
        }
    }
}