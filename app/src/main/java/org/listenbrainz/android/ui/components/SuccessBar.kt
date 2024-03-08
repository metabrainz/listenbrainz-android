package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.R

@Composable
fun SuccessBar(
    resId : Int?,
    onMessageShown: () -> Unit,
    snackbarState : SnackbarHostState
) {
    val context = LocalContext.current;
    LaunchedEffect(resId) {
        if (resId != null) {
            delay(4000)
            onMessageShown()
        }
    }

    AnimatedVisibility(
        visible = resId != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LaunchedEffect(key1 = resId){
            if(resId != null){
                snackbarState.showSnackbar(context.getString(resId))
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun SuccessBarPreview() {
    ListenBrainzTheme {
        SuccessBar(resId = R.string.about_title , onMessageShown = {} , snackbarState = SnackbarHostState())
    }
}