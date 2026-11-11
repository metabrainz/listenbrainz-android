package org.listenbrainz.shared.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme

@Composable
fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    ListenBrainzTheme {
        Box(
            modifier = modifier.background(ListenBrainzTheme.colorScheme.background),
            content = content
        )
    }
}