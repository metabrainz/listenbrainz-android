package org.listenbrainz.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import org.listenbrainz.android.R

@Composable
fun Loader() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.headphone_meb_loading))
    LottieAnimation(composition)
}

@Preview
@Composable
fun LoaderPreview() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.headphone_meb_loading))
    LottieAnimation(composition)
}