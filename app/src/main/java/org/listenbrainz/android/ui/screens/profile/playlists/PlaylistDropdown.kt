package org.listenbrainz.android.ui.screens.profile.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PlaylistDropdownMenu(expanded: Boolean, onDismiss: () -> Unit, onItemClick: (PlaylistDropdownItems) -> Unit,
                         isPrivateAllowed: Boolean) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(ListenBrainzTheme.colorScheme.background)
    ) {
        PlaylistDropdownItems.entries.forEach { item ->
            if(item.isPrivate && !isPrivateAllowed) return@forEach
            DropdownMenuItem(
                text = { Text(text = item.title) },
                leadingIcon = {
                    Icon(painter = painterResource(item.image),
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
    val image: Int,
    val isPrivate: Boolean
){
    DUPLICATE(title = "Duplicate", image = R.drawable.playlist_save, isPrivate = false),
    DELETE(title = "Delete", image = R.drawable.ic_delete, isPrivate = true),
    SHARE(title = "Share", image = R.drawable.playlist_share_btn, isPrivate = false),
    EDIT(title = "Edit", image = R.drawable.ic_review, isPrivate = true),
}
