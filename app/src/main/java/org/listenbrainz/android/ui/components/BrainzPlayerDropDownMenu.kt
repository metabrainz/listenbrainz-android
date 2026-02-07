package org.listenbrainz.android.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun BrainzPlayerDropDownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit = {},
    onAddToNewPlaylist: (() -> Unit)? = null,
    onAddToExistingPlaylist: (() -> Unit)?  = null,
    onPlayNext: (() -> Unit )?= null,
    onAddToQueue: (() -> Unit)? = null,
    onShareAudio: (() -> Unit)? = null,
    showShareOption: Boolean = false
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if(onAddToNewPlaylist!=null){
            DropdownMenuItem(onClick = {
                onAddToNewPlaylist()
                onDismiss()
            }, text = { Text(text = "Add to new playlist") })
        }

        if(onAddToExistingPlaylist!=null){
            DropdownMenuItem(onClick = {
                onAddToExistingPlaylist()
                onDismiss()
            }, text = { Text(text = "Add to existing playlist") })
        }

        if(onPlayNext!=null){
            DropdownMenuItem(onClick = {
                onPlayNext()
                onDismiss()
            }, text = { Text(text = "Play next") })
        }

        if(onAddToQueue!=null){
            DropdownMenuItem(onClick = {
                onAddToQueue()
                onDismiss()
            }, text = { Text(text = "Add to queue") })
        }
        if (showShareOption) {
            if(onShareAudio!=null){
                DropdownMenuItem(onClick = {
                    onShareAudio()
                    onDismiss()
                }, text = { Text(text = "Share") })
            }
        }
    }
}

