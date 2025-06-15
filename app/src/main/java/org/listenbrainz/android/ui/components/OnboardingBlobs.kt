package org.listenbrainz.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Preview(showBackground = true)
@Composable
fun OnboardingBlobs(modifier: Modifier = Modifier, isRotated: Boolean = false){
    Row(modifier = modifier.fillMaxWidth()
        .graphicsLayer{
            if(isRotated) {
                rotationY = 180f
                rotationX = 180f
            }
        }
        .widthIn(max = 600.dp),
        horizontalArrangement = Arrangement.Center) {
        Image(painter = painterResource(R.drawable.onboard_blob_2), contentDescription = "Blob 1",)
        Image(painter = painterResource(R.drawable.onboard_blob_1), contentDescription = "Blob 2",
            modifier = Modifier.graphicsLayer{
                translationX = 180f
                translationY = -400f
            })
        Image(painter = painterResource(R.drawable.onboard_blob_3), contentDescription = "Blob 3",
            modifier = Modifier.graphicsLayer{
                translationY = 300f
            })
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingBlobsPreview(){
    ListenBrainzTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                OnboardingBlobs()
                OnboardingBlobs(isRotated = true)
            }
        }
    }
}
