package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun ReviewDisabledDialog(
    onDismiss: () -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current
) {
    BaseDialog(
        onDismiss = onDismiss,
        
        title = {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_critiquebrainz_logo),
                contentDescription = "CritiqueBrainz Logo"
            )
        },
        
        content = {
            val annotatedString = buildAnnotatedString {
                withStyle(
                    ListenBrainzTheme.textStyles.dialogText.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                ) {
                    append("Before you can submit reviews for your Listens to ")
                }
    
                pushStringAnnotation("redirect", "https://critiquebrainz.org/")
                withStyle(
                    ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.lbSignature)
                ) {
                    append("CritiqueBrainz")
                }
                pop()
    
                withStyle(
                    ListenBrainzTheme.textStyles.dialogText.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                ) {
                    append(", you must ")
                }
    
                withStyle(
                    ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                ) {
                    append("connect to your CritiqueBrainz account")
                }
    
                withStyle(
                    ListenBrainzTheme.textStyles.dialogText.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                ) {
                    append(" with ListenBrainz.")
                }
            }
            
            ClickableText(
                text = annotatedString
            ) { offset ->
                annotatedString.getStringAnnotations("redirect", offset, offset).firstOrNull()?.let{
                    uriHandler.openUri(it.item)
                }
                
            }
            
            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideDialog))
    
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(
                        ListenBrainzTheme.textStyles.dialogText.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                    ){
                        append("You can connect to your CritiqueBrainz account by visiting the ")
                    }
    
                    withStyle(
                        ListenBrainzTheme.textStyles.dialogTextBold.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                    ){
                        append("music services page")
                    }
    
                    withStyle(
                        ListenBrainzTheme.textStyles.dialogText.toSpanStyle()
                        .copy(ListenBrainzTheme.colorScheme.text)
                    ){
                        append(".")
                    }
                },
            ){}
        },
        
        footer = {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                DialogPositiveButton(text = "Connect to CritiqueBrainz") {
                    uriHandler.openUri("https://listenbrainz.org/profile/music-services/details/")
                    onDismiss()
                }
            }
        }
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewDisabledDialogPreview(){
    ListenBrainzTheme {
        ReviewDisabledDialog(
            onDismiss = {}
        )
    }
}