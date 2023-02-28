package org.listenbrainz.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.util.AlbumsData
import org.listenbrainz.android.util.SongData

@Composable
fun BpProgressIndicator(
    screen: BrainzNavigationItem,
    listIsEmpty : Boolean = when (screen){
        BrainzNavigationItem.Albums -> !AlbumsData.albumsOnDevice
        BrainzNavigationItem.Artists -> !AlbumsData.albumsOnDevice
        BrainzNavigationItem.Songs -> !SongData.songsOnDevice
        else -> true
    }
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (listIsEmpty){
            Text(text = "Seems like your music library is empty.")
        }else{
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.inverseOnSurface)
        
                Spacer(modifier = Modifier.width(8.dp))
        
                Text(
                    text = stringResource(id = R.string.bp_loading_text),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}