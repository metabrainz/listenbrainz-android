package org.listenbrainz.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.ProvideLBButtonStyle
import org.listenbrainz.android.util.ProvideOnLBSignatureColors

@Composable
fun ButtonLB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(ListenBrainzTheme.shapes.lbButton)
            .background(ListenBrainzTheme.colorScheme.lbSignature)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        ProvideLBButtonStyle {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}