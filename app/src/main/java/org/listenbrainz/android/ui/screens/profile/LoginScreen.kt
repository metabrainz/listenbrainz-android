package org.listenbrainz.android.ui.screens.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.onboarding.auth.login.ListenBrainzLogin
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun LoginScreen(
    navigateToUserProfile: suspend () -> Unit,
    navigateToCreateAccount: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var startLogin by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val comp by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.login))
        LottieAnimation(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f),
            composition = comp,
            iterations = LottieConstants.IterateForever,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                startLogin = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ListenBrainzTheme.colorScheme.lbSignature)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                text = stringResource(id = R.string.login),
                color = ListenBrainzTheme.colorScheme.onLbSignature,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Text(
            text = stringResource(id = R.string.login_prompt),
            color = ListenBrainzTheme.colorScheme.text,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 10.dp, end = 10.dp)
        )
    }

    if (startLogin) {
        ListenBrainzLogin(
            modifier = Modifier.background(ListenBrainzTheme.colorScheme.background),
            onCreateAccountClicked = navigateToCreateAccount
        ) {
            scope.launch {
                navigateToUserProfile()
                startLogin = false
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    ListenBrainzTheme {
        LoginScreen(
            navigateToUserProfile = {},
            navigateToCreateAccount = {}
        )
    }
}