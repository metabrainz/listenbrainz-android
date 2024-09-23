package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.model.UserListUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/** State of this button changes optimistically and will revert back if something goes wrong. This inversion of state is determined by
 * the resulting flow returned by [onClick].
 * @param cornerRadius Corner radius of the button.
 * @param isFollowedState Follow state of the user. Needs to be a subtype of [State]. Consider using [UserListUiState] for this
 * purpose.
 * @param scope Usually, there will be a lot of follow buttons in a view, it is advised to pass one scope
 * and avoid creating unnecessary scopes for each button that exists.
 * @param onClick This param must perform the follow-unfollow function and return a flow which tells
 * the composable whether the operation was successful or not. The flow **must** complete so that a lot of
 * streams are not accumulated. */
@Composable
fun FollowButton(
    modifier: Modifier = Modifier,
    isFollowedState: Boolean = false,
    height: Dp = 30.dp,
    fontSize: TextUnit = (height.value/2 - 1).sp,
    cornerRadius: Dp = 6.dp,
    buttonColor: Color = ListenBrainzTheme.colorScheme.lbSignature,
    onClick: () -> Unit,
) {
    
    val transition = updateTransition(
        targetState = isFollowedState,
        label = "Parent"
    )
    
    val colorAlpha by transition.animateFloat(label = "Alpha") { isFollowedByUser ->
        if (isFollowedByUser) 0f else 1f
    }
    
    Surface(
        modifier = modifier
            .height(height)
            .width(height * (2.5f))
            .graphicsLayer { alpha = colorAlpha },
        color = buttonColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {}
    
    Surface(
        modifier = modifier
            .height(height)
            .width(height * (2.5f))
            .clickable { onClick() },
        border = BorderStroke(2.dp, buttonColor),
        shape = RoundedCornerShape(cornerRadius),
        color = Color.Unspecified
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (isFollowedState) "Following" else "Follow",
                color = if (isFollowedState) ListenBrainzTheme.colorScheme.lbSignature else ListenBrainzTheme.colorScheme.onLbSignature,
                fontWeight = FontWeight.Medium,
                fontSize = fontSize
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FollowButtonPreview() {
    ListenBrainzTheme {
        FollowButton(isFollowedState = true){}
    }
}