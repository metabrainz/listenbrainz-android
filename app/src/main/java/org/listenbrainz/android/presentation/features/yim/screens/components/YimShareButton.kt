package org.listenbrainz.android.presentation.features.yim.screens.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R

@Composable
fun YimShareButton(isRedTheme: Boolean, modifier: Modifier = Modifier) {
    IconButton(
        modifier = modifier,
        onClick = {
            /*TODO: Implement share functionality.*/
        }
    ) {
        Icon(
            painter = painterResource(
                id = if (isRedTheme) R.drawable.yim_share_yellow else R.drawable.yim_share_red
            ),
            contentDescription = "Share your Year in Music",
            tint = Color.Unspecified,
            modifier = Modifier
                .clip(CircleShape)
                .size(50.dp)
        )
    }
}