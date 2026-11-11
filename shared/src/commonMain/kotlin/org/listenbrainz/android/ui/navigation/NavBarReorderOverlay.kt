package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.BottomNavDefaults
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.nav_reorder_hint
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableRow

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

    NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None)) {
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
                text = stringResource(Res.string.nav_reorder_hint),
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DummyAdaptiveNavBar(
            items = mutableItems,
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
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = ListenBrainzTheme.colorScheme.nav
    val onSettle: (Int, Int) -> Unit = { from, to -> items.add(to, items.removeAt(from)) }
    // A new list after each move resets the reorderable state, which is remembered by the list
    val itemsSnapshot = items.toList()

    // Laid out like AdaptiveNavigationBar, with the same item composables
    if (isLandscape) {
        NavigationRail(
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            ReorderableColumn(
                list = itemsSnapshot,
                onSettle = onSettle,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { _, item, _ ->
                ReorderableItem {
                    AppNavigationRailItem(
                        item = item,
                        selected = false,
                        onClick = {},
                        indicatorColor = backgroundColor,
                        modifier = Modifier.draggableHandle()
                    )
                }
            }
        }
    } else {
        NavigationBar(
            modifier = modifier,
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            ReorderableRow(
                list = itemsSnapshot,
                onSettle = onSettle,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { _, item, _ ->
                // Takes the share NavigationBarItem's own weight would inside NavigationBar
                ReorderableItem(modifier = Modifier.weight(1f)) {
                    AppNavigationBarItem(
                        item = item,
                        selected = false,
                        onClick = {},
                        indicatorColor = backgroundColor,
                        modifier = Modifier.fillMaxWidth().draggableHandle()
                    )
                }
            }
        }
    }
}
