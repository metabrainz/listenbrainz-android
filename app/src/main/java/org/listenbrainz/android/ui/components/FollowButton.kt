package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/**
 * @param cornerRadius Acts as corner radius as well as border width.
 * @param onClick This param must perform the follow-unfollow function and return a flow which tells
 * the composable whether the operation was successful or not. The flow **must** complete so that a lot of
 * streams are not accumulated.
 * @param scope Usually, there will be a lot of follow buttons in a view, it is advised to pass one scope
 * and avoid creating unnecessary scopes for each button that exists.*/
@Composable
fun FollowButton(
    modifier: Modifier = Modifier,
    isFollowed: Boolean = false,
    height: Dp = 30.dp,
    cornerRadius: Dp = 2.dp,
    scope: CoroutineScope,
    buttonColor: Color = ListenBrainzTheme.colorScheme.lbSignature,
    onClick: suspend () -> Flow<Boolean>,
) {
    var isFollowedState by remember { mutableStateOf(isFollowed) }
    
    val transition = updateTransition(
        targetState = isFollowedState,
        label = "Parent"
    )
    
    val colorAlpha by transition.animateColor(label = "Color") { isFollowedByUser ->
        if (isFollowedByUser) Color.Unspecified else ListenBrainzTheme.colorScheme.lbSignature
    }
    
    Surface(
        modifier = modifier
            .height(height)
            .width(height * (2.5f))
            .clickable {
                fun invertState() { isFollowedState = !isFollowedState }
                
                // Optimistically invert state.
                invertState()
                
                scope.launch(Dispatchers.IO) {
                    onClick().collect { isSuccessful ->
                        if (!isSuccessful)
                            // Invert state again if operation is unsuccessful.
                            invertState()
                    }
                }
                
            }
            .drawWithCache {
        
                /** This is done to avoid recompositions.*/
        
                /** Corner Radius*/
                val x = cornerRadius.toPx()
        
                val w = size.width
                val h = size.height
        
                val path = Path().apply {
                    moveTo(x, 2 * x)
                    quadraticBezierTo(x, x, 2 * x, x)
                    lineTo(w - 2 * x, x)
                    quadraticBezierTo(w - x, x, w - x, 2 * x)
                    lineTo(w - x, h - 2 * x)
                    quadraticBezierTo(w - x, h - x, w - 2 * x, h - x)
                    lineTo(2 * x, h - x)
                    quadraticBezierTo(x, h - x, x, h - 2 * x)
                    close()
                }
        
                onDrawBehind {
            
                    clipPath(path, clipOp = ClipOp.Difference) {
                        drawRoundRect(color = buttonColor, cornerRadius = CornerRadius(x, x))
                    }
            
                    drawRoundRect(color = colorAlpha, cornerRadius = CornerRadius(x, x))
                }
            },
        color = Color.Unspecified,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier,
                text = if (isFollowedState) "Following" else "Follow",
                color = if (isFollowedState) ListenBrainzTheme.colorScheme.lbSignature else ListenBrainzTheme.colorScheme.onLbSignature,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FollowButtonPreview() {
    ListenBrainzTheme {
        FollowButton(isFollowed = true, cornerRadius = 2.dp, scope = rememberCoroutineScope()){ flow {}}
    }
}