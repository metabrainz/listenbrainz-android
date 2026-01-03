package org.listenbrainz.android.ui.screens.feed.events

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.viewmodel.LikeState

@Composable
fun LikeDislikeButton(
    likeState: LikeState,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    val icon = when (likeState) {
        LikeState.NEUTRAL -> Icons.Outlined.FavoriteBorder
        LikeState.LIKED -> Icons.Filled.Favorite
        LikeState.DISLIKED -> Icons.Filled.HeartBroken
    }

    val targetColor = when (likeState) {
        LikeState.NEUTRAL -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        LikeState.LIKED -> MaterialTheme.colorScheme.primary
        LikeState.DISLIKED -> MaterialTheme.colorScheme.error
    }

    val animatedColor by animateColorAsState(targetValue = targetColor)

    Icon(
        imageVector = icon,
        contentDescription = when (likeState) {
            LikeState.NEUTRAL -> "No rating"
            LikeState.LIKED -> "Liked"
            LikeState.DISLIKED -> "Disliked"
        },
        tint = animatedColor,
        modifier = modifier
            .size(26.dp)
            .combinedClickable(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTap()
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
    )
}
