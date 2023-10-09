package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
/**
 * @param isCritiqueBrainzLinked null means couldn't fetch results from server.*/
fun ReviewDialog(
    trackName: String?,
    artistName: String?,
    releaseName: String?,
    onDismiss: () -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onSubmit: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) -> Unit
) {
    var isLinked by rememberSaveable { mutableStateOf<Boolean?>(null) }
    
    LaunchedEffect(Unit){
        val result = isCritiqueBrainzLinked()
        if (result == null) {
            // Null means our API request to server has failed.
            onDismiss()
            return@LaunchedEffect
        }
        isLinked = result
    }
    
    when (isLinked) {
        true -> ReviewEnabledDialog(
            trackName = trackName,
            artistName = artistName,
            releaseName = releaseName,
            onDismiss = onDismiss,
            onSubmit = onSubmit
        )
        false -> ReviewDisabledDialog(onDismiss = onDismiss)
        null -> Dialog(onDismissRequest = onDismiss) {
            CircularProgressIndicator(
                color = ListenBrainzTheme.colorScheme.lbSignatureInverse
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ReviewDialogPreview(){
    ListenBrainzTheme {
        ReviewDialog(
            trackName = "Gucci Chick",
            artistName = "Babbulicious",
            releaseName = null,
            onDismiss = { /*TODO*/ },
            isCritiqueBrainzLinked = {
                delay(500)
                null
            },
            onSubmit = { _,_,_,_ -> }
        )
    }
}