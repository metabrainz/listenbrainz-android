package org.listenbrainz.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayPauseIcon(icon: ImageVector, viewModel: BrainzPlayerViewModel, modifier: Modifier = Modifier, tint : Color = Color.Black) {
    AnimatedContent(
        targetState = icon,
        transitionSpec = {
            when (targetState) {
                Icons.Rounded.PlayArrow -> {
                    slideInVertically { height -> -height } + fadeIn() with
                            slideOutHorizontally { height -> height } + fadeOut()
                }
                else -> {
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutHorizontally{ height -> -height } + fadeOut()
                }
            }.using(SizeTransform(false))
        }

    ) {
        Icon(imageVector = it, contentDescription = "",  modifier.clickable {
            viewModel.playOrToggleSong(viewModel.currentlyPlayingSong.value.toSong,true)
        }
        ,tint = tint)
    }
}