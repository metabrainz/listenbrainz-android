package org.listenbrainz.android.ui.screens.artist

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ArtistScreen(
    artistMbid: String
) {
    Text(text = artistMbid)
}