package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.listenbrainz.shared.model.AppNavigationItem

/** Shared by the navigation bar and its reorder overlay, so both lay items out the same way. */
@Composable
fun RowScope.AppNavigationBarItem(
    item: AppNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current
) {
    NavigationBarItem(
        modifier = modifier,
        icon = { NavIcon(item, selected, contentColor) },
        label = { NavLabel(item, contentColor) },
        alwaysShowLabel = true,
        selected = selected,
        colors = NavigationBarItemDefaults.colors(indicatorColor = indicatorColor),
        onClick = onClick
    )
}

/** Shared by the navigation rail and its reorder overlay, so both lay items out the same way. */
@Composable
fun AppNavigationRailItem(
    item: AppNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current
) {
    NavigationRailItem(
        modifier = modifier,
        icon = { NavIcon(item, selected, contentColor) },
        label = { NavLabel(item, contentColor) },
        alwaysShowLabel = true,
        selected = selected,
        colors = NavigationRailItemDefaults.colors(indicatorColor = indicatorColor),
        onClick = onClick
    )
}

@Composable
private fun NavIcon(item: AppNavigationItem, selected: Boolean, contentColor: Color) {
    Icon(
        painter = painterResource(if (selected) item.iconSelected else item.iconUnselected),
        contentDescription = item.title,
        tint = contentColor
    )
}

@Composable
private fun NavLabel(item: AppNavigationItem, contentColor: Color) {
    Text(
        text = item.title,
        color = contentColor,
    )
}
