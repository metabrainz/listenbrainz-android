package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.repository.social.SocialRepository.Companion.getPinDateString
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PinDialog(
    trackName: String,
    artistName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var blurbContent by rememberSaveable {
        mutableStateOf("")
    }
    BaseDialog(
        onDismiss = onDismiss,
        
        title = {
            Text(
                text = buildAnnotatedString {
                    withStyle(ListenBrainzTheme.textStyles.dialogTitle.toSpanStyle()){
                        append("Pin this track to your profile")
                    }
                },
                color = ListenBrainzTheme.colorScheme.text
            )
        },
        
        content = {
    
            Text(
                modifier = Modifier.padding(bottom = ListenBrainzTheme.paddings.insideDialog),
                text = buildAnnotatedString {
                    withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle()){
                        append("Why do you love ")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()){
                        append("$trackName by $artistName")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle()){
                        append("? (Optional)")
                    }
                },
                color = ListenBrainzTheme.colorScheme.text
            )
            
            DialogTextField(
                modifier = Modifier.height(160.dp),
                value = blurbContent,
                onValueChange = {
                    if (it.length <= 280){
                        blurbContent = it
                    } },
                placeholder = "Let your followers know why you are showcasing this track..."
            )
    
            
            DialogText(modifier = Modifier
                .padding(vertical = ListenBrainzTheme.paddings.insideDialog)
                .align(Alignment.End),
                text = "${blurbContent.length} / 280"
            )
            
            DialogText("Pinning this track will replace any track currently pinned.")
            
            Text(
                modifier = Modifier.padding(top = ListenBrainzTheme.paddings.insideDialog),
                text = buildAnnotatedString {
                    withStyle(ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()){
                        append("$trackName by $artistName ")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle()){
                        append("will be unpinned from your profile in")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()){
                        append(" one week")
                    }
                    withStyle(ListenBrainzTheme.textStyles.dialogText.toSpanStyle()){
                        append(", on ${getPinDateString()}")
                    }
                },
                color = ListenBrainzTheme.colorScheme.text
            )
            
        },
        
        footer = {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
        
                DialogNegativeButton(onDismiss = onDismiss)
        
                Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.adjacentDialogButtons))
        
                DialogPositiveButton(text = "Pin track") {
                    onSubmit(blurbContent)
                    onDismiss()
                }
            }
        }
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PinDialogPreview() {
    ListenBrainzTheme {
        PinDialog(
            trackName = "Gucci Chick",
            artistName = "Babbulicious",
            onDismiss = {},
            onSubmit = {}
        )
    }
}