package org.listenbrainz.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_yellow
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

@Composable
fun OnboardingYellowButton(modifier: Modifier = Modifier,
                           text: String = "Next",
                           onClick: () -> Unit) {
    Button(onClick = onClick,
        modifier = modifier,
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = lb_yellow
        )) {
        Text(text, color = ListenBrainzTheme.colorScheme.text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingYellowButtonPreview(){
    ListenBrainzTheme {
        OnboardingYellowButton(
            text = "Next",
            onClick = {}
        )
    }
}