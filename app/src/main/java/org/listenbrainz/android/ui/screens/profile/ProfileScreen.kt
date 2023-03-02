package org.listenbrainz.android.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.listenbrainz.android.R

@Composable
fun ProfileScreen() {
    var isLottiePlaying by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box() {
            val comp by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.login))
            LottieAnimation(
                composition = comp,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.requiredHeightIn(max = 400.dp),
                isPlaying = isLottiePlaying
            )
        }

        Button(
            onClick = { isLottiePlaying = !isLottiePlaying },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 10.dp, end = 10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface)
        ) {
            Text(text = "Login", color = Color.White)
        }

        Text(
            text = stringResource(id = R.string.login_prompt),
            color = MaterialTheme.colors.surface,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 50.dp, start = 5.dp, end = 5.dp)
        )
    }
}

