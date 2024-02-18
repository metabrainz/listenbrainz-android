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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.ui.theme.ListenBrainzTheme


@Composable
fun SuccessBar(
    message : String?,
    onMessageShown: () -> Unit,
    snackbarState : SnackbarHostState
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(4000)
            onMessageShown()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LaunchedEffect(key1 = message){
            if(message != null){
                snackbarState.showSnackbar(message)
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SuccessBarPreview() {
    ListenBrainzTheme {
        ErrorBar(error = ResponseError.NETWORK_ERROR) {}
    }
}