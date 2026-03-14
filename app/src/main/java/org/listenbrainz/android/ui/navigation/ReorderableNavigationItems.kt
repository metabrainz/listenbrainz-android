import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.util.iconUnselected
import org.listenbrainz.shared.model.AppNavigationItem
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState


@Composable
fun NavIcon(item: AppNavigationItem) {
    Icon(
        painter = painterResource(id = item.iconUnselected),
        modifier = Modifier
            .size(24.dp)
            .padding(vertical = 4.dp),
        contentDescription = item.title,
        tint = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun NavLabel(item: AppNavigationItem) {
    Text(
        text = item.title,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp
    )
}

@Composable
fun ReorderableNavRail(
    items: SnapshotStateList<AppNavigationItem>,
    lazyListState: LazyListState,
    reorderableLazyListState: ReorderableLazyListState,
    backgroundColor: Color
) {

    LazyColumn(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        state = lazyListState
    ) {
        items(items, key = { it.route }) { item ->
            ReorderableItem(reorderableLazyListState, key = item.route) { _ ->
                NavigationRailItem(
                    modifier = Modifier.draggableHandle(),
                    icon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .safeContentPadding()
                                .fillMaxWidth()
                        ) {
                            NavIcon(item)
                            NavLabel(item)
                        }
                    },
                    alwaysShowLabel = false,
                    selected = false,
                    colors = NavigationRailItemDefaults.colors(
                        indicatorColor = backgroundColor
                    ),
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun ReorderableBottomNavBar(
    items: SnapshotStateList<AppNavigationItem>,
    lazyListState: LazyListState,
    reorderableLazyListState: ReorderableLazyListState,
    scope: RowScope
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        state = lazyListState,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(items, key = { it.route }) { item ->
            ReorderableItem(reorderableLazyListState, key = item.route) { _ ->
                scope.let {
                    it.BottomNavigationItem(
                        icon = { NavIcon(item) },
                        label = { NavLabel(item) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = colorResource(id = R.color.gray),
                        alwaysShowLabel = true,
                        selected = false,
                        onClick = {},
                        modifier = Modifier.navigationBarsPadding().draggableHandle()
                    )
                }
            }
        }
    }
}