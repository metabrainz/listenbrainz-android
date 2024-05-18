package org.listenbrainz.android.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun BrainzPlayerDropDownMenu(
    expanded : Boolean ,
    onDismiss : () -> Unit = {},
    onAddToNewPlaylist : () -> Unit = {},
    onAddToExistingPlaylist : () -> Unit = {},
    onPlayNext : () -> Unit = {},
    onAddToQueue : () -> Unit = {},
){
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(onClick = {
            onAddToNewPlaylist()
            onDismiss()
        }, text = {Text(text = "Add to new playlist")})
        DropdownMenuItem(onClick = {
            onAddToExistingPlaylist()
            onDismiss()
        }, text = {Text(text = "Add to existing playlist")})
        DropdownMenuItem(onClick = {
            onPlayNext()
            onDismiss()
        }, text = {Text(text = "Play next")})
        DropdownMenuItem(onClick = {
            onAddToQueue()
            onDismiss()
        }, text = {Text(text = "Add to queue")})
    }

}
