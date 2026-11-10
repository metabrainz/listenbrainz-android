package org.listenbrainz.android.ui.components.dialogs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun DialogText(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false
) {
    Text(
        modifier = modifier,
        text = text,
        color = ListenBrainzTheme.colorScheme.text,
        style = if (bold) ListenBrainzTheme.textStyles.dialogTextBold else ListenBrainzTheme.textStyles.dialogText
    )
}