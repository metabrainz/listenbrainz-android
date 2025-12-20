package org.listenbrainz.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_yellow
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayPauseIcon(
    icon: ImageVector,
    viewModel: BrainzPlayerViewModel,
    modifier: Modifier = Modifier,
    tint: Color = Color.Black
) {
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
                            slideOutHorizontally { height -> -height } + fadeOut()
                }
            }.using(SizeTransform(false))
        }

    ) {
        Icon(imageVector = it, contentDescription = "", modifier.clickable {
            viewModel.playOrToggleSong(viewModel.currentlyPlayingSong.value.toSong, true)
        }, tint = tint)
    }
}


@Composable
fun OnboardingGrayButton(
    modifier: Modifier = Modifier,
    text: String = "Next",
    icon: Int? = null,
    fontSize: Int = 18,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            if (isEnabled) {
                onClick()
            }
        },
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = ListenBrainzTheme.shapes.listenCardSmall,
                spotColor = Color.Black.copy(alpha = 0.25f),
                ambientColor = Color.Black.copy(alpha = 0.12f)
            ),
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) Color(0xFFE0DCDC) else Color(0xFFE0DCDC).copy(alpha = 0.6f),
            disabledContainerColor = Color(0xFFE0DCDC).copy(alpha = 0.6f)
        ),
        enabled = isEnabled
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (isEnabled) Color.Black else Color.Black.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize.sp
            )
            icon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = if (isEnabled) Color.Black else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingYellowButton(
    modifier: Modifier = Modifier,
    text: String = "Next",
    icon: Int? = null,
    fontSize: Int = 18,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    onClickWhileDisabled: ()-> Unit = {}
) {
    Button(
        onClick = {
            if (isEnabled) {
                onClick()
            }else{
                onClickWhileDisabled()
            }
        },
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = ListenBrainzTheme.shapes.listenCardSmall,
                spotColor = Color.Black.copy(alpha = 0.4f),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            )
            .padding(2.dp)
            .alpha(if (isEnabled) 1f else 0.6f)
        ,
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) lb_yellow else lb_yellow.copy(alpha = 0.6f),
            disabledContainerColor = lb_yellow.copy(alpha = 0.6f)
        ),
        //Not using enabled = isEnabled here because it causes the button to be disabled, so onClick won't work
//        enabled = isEnabled
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (isEnabled) ListenBrainzTheme.colorScheme.text else ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize.sp
            )
            icon?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = if (isEnabled) ListenBrainzTheme.colorScheme.text else ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingYellowButtonPreview() {
    ListenBrainzTheme {
        Column {
            OnboardingYellowButton(
                text = "Next",
                onClick = {}
            )
            Spacer(Modifier.height(16.dp))
            OnboardingGrayButton(
                text = "Next",
                onClick = {}
            )
        }
    }
}