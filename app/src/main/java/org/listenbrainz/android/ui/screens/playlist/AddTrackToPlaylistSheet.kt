package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.recordingSearch.RecordingData

@Composable
fun AddTrackToPlaylist(
    modifier: Modifier,
    playlistDetailUIState: PlaylistDetailUIState,
    onTrackSelect: (RecordingData) -> Unit,
    onQueryChange: (String) -> Unit,
    onDismiss: ()->Unit
){

}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddToPlaylistPreview(){

}
