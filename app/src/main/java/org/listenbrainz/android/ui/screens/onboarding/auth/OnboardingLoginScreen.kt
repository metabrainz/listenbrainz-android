package org.listenbrainz.android.ui.screens.onboarding.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.components.DiagonalCutShape
import org.listenbrainz.android.ui.components.OnboardingBlobs
import org.listenbrainz.android.ui.components.OnboardingGrayButton
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.onboardingGradient

@Composable
fun OnboardingLoginScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    LoginScreenBase(
        onLoginClick = onLoginClick,
        onNewAccountClick = onCreateAccountClick
    )
}

@Composable
private fun LoginScreenBase(
    onLoginClick: () -> Unit,
    onNewAccountClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text("Login")
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(brush = onboardingGradient, shape = DiagonalCutShape(cutHeight = 240f)),
        )
        OnboardingBlobs()
        Row(modifier = Modifier.align(Alignment.Center)) {
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = 600f
                    }
            ) {
                Text(
                    "Get Started with \nListenBrainz",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "To continue, log in with your MusicBrainz account or create a new one.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))
                Row {
                    OnboardingYellowButton(
                        text = "Create Account",
                        onClick = onNewAccountClick)
                    Spacer(Modifier.width(8.dp))
                    OnboardingGrayButton(text = "Login", onClick = {
                        onLoginClick()
                    })
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
fun LoginScreenPreview() {
    ListenBrainzTheme {
        LoginScreenBase(
            onLoginClick = {},
            onNewAccountClick = {}
        )
    }
}