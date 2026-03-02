package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.search.userSearch.UserListUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/** State of this button changes optimistically and will revert back if something goes wrong. This inversion of state is determined by
 * the resulting flow returned by [onClick].
 * @param isFollowedState Follow state of the user. Needs to be a subtype of [State]. Consider using [UserListUiState] for this
 * purpose.
 * @param onClick This param must perform the follow-unfollow function and return a flow which tells
 * the composable whether the operation was successful or not. The flow **must** complete so that a lot of
 * streams are not accumulated. */
@Composable
fun FollowButton(
    modifier: Modifier = Modifier,
    isFollowedState: Boolean = false,
    buttonColor: Color = ListenBrainzTheme.colorScheme.lbSignature,
    followedStateTextColor: Color = ListenBrainzTheme.colorScheme.lbSignature,
    unfollowedStateTextColor: Color = ListenBrainzTheme.colorScheme.onLbSignature,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isFollowedState) Color.Transparent else buttonColor
    )
    val shape = ListenBrainzTheme.shapes.lbButton

    Box(
        modifier = modifier
            .clip(shape = shape)
            .clickable(onClick = onClick)
            .background(color = bgColor, shape = shape)
            .border(border = BorderStroke(2.dp, buttonColor), shape = shape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFollowedState) "Following" else "Follow",
            color = if (isFollowedState) followedStateTextColor else unfollowedStateTextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FollowButtonPreview() {
    ListenBrainzTheme {
        var state by remember { mutableStateOf(true) }
        FollowButton(isFollowedState = state) { state = !state }
    }
}