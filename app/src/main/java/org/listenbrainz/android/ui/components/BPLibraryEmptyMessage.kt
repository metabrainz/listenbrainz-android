package org.listenbrainz.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BPLibraryEmptyMessage(modifier: Modifier = Modifier) {
    Text(
        text = "Your music library is empty.",
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface
    )
}