package org.listenbrainz.android.util

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ListenBrainzTheme {
        Surface(
            modifier = modifier,
            color = ListenBrainzTheme.colorScheme.background,
            content = content
        )
    }
}