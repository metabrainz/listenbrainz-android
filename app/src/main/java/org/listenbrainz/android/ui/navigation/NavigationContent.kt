package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun NavigationContent(
    item: AppNavigationItem,
    backgroundColor: Color = ListenBrainzTheme.colorScheme.nav,
    contentColor: Color? = null,
    selected: Boolean,
    onItemClick: () -> Unit,
    isLandscape: Boolean,
    scope: RowScope?
) {
    val navIcon = @Composable {
        Icon(
            painter = painterResource(
                id = if (selected) item.iconSelected else item.iconUnselected
            ),
            modifier = Modifier
                .size(24.dp)
                .padding(vertical = 4.dp),
            contentDescription = item.title,
            tint = contentColor ?: MaterialTheme.colorScheme.onSurface
        )
    }

    val navLabel = @Composable {
        Text(
            text = item.title,
            color = contentColor?: MaterialTheme.colorScheme.onSurface,
        )
    }

    if (isLandscape) {
        NavigationRailItem(
            icon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .safeContentPadding()
                        .fillMaxWidth()
                ) {
                    navIcon()
                    navLabel()
                }
            },
            alwaysShowLabel = false,
            selected = selected,
            colors = NavigationRailItemDefaults.colors(
                indicatorColor = backgroundColor
            ),
            onClick = onItemClick
        )
    } else {
        scope?.let {
            it.BottomNavigationItem(
                icon = { navIcon() },
                label = { navLabel() },
                selectedContentColor = contentColor?: MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = contentColor ?: colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = selected,
                onClick = onItemClick,
                modifier = Modifier.navigationBarsPadding()
            )
        }

    }
}