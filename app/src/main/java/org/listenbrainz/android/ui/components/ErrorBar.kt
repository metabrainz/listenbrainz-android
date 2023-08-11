package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/** Error bar to be used to show [ResponseError] if any. ResponseError should be mutable.
 * @param error The error of type [ResponseError] to be shown. Error is shown is error is non-null.
 * @param onErrorShown This function should set the error value to null and is executed after the error
 * bar hides again.*/
@Composable
fun ErrorBar(
    error: ResponseError?,
    onErrorShown: () -> Unit
) {
    
    LaunchedEffect(error) {
        if (error != null) {
            delay(4000)
            onErrorShown()
        }
    }
    
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ListenBrainzTheme.colorScheme.lbSignature),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = error?.toast() ?: "",
                modifier = Modifier.padding(vertical = 4.dp),
                color = ListenBrainzTheme.colorScheme.onLbSignature,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ErrorBarPreview() {
    ListenBrainzTheme {
        ErrorBar(error = ResponseError.NETWORK_ERROR) {}
    }
}