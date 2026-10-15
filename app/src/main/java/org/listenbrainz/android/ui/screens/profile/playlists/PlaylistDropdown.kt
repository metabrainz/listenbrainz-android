package org.listenbrainz.android.ui.screens.profile.playlists

import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.foundation.background
import org.jetbrains.compose.resources.painterResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_delete
import org.listenbrainz.shared.resources.ic_review
import org.listenbrainz.shared.resources.playlist_duplicate_svg
import org.listenbrainz.shared.resources.playlist_share_btn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme


/**
 * Composable for Playlist Dropdown Menu
 * @param expanded Boolean - Tells whether the dropdown menu is expanded
 * @param onDismiss () -> Unit - Function to be called when dropdown menu is dismissed
 * @param onItemClick (PlaylistDropdownItems) -> Unit - Function to be called when dropdown item is clicked
 * @param isPrivateAllowed Boolean - Tells whether the private playlist is allowed
 */
@Composable
fun PlaylistDropdownMenu(
    expanded: Boolean, onDismiss: () -> Unit, onItemClick: (PlaylistDropdownItems) -> Unit,
    isPrivateAllowed: Boolean
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(ListenBrainzTheme.colorScheme.background)
    ) {
        PlaylistDropdownItems.entries.forEach { item ->
            if (item.isPrivate && !isPrivateAllowed) return@forEach
            DropdownMenuItem(
                text = { Text(text = item.title) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(item.image),
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onItemClick(item) }
            )
        }
    }
}


enum class PlaylistDropdownItems(
    val title: String,
    val image: DrawableResource,
    val isPrivate: Boolean
) {
    DUPLICATE(title = "Duplicate", image = Res.drawable.playlist_duplicate_svg, isPrivate = false),
    DELETE(title = "Delete", image = Res.drawable.ic_delete, isPrivate = true),
    SHARE(title = "Share", image = Res.drawable.playlist_share_btn, isPrivate = false),
    EDIT(title = "Edit", image = Res.drawable.ic_review, isPrivate = true),
}
