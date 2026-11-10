package org.listenbrainz.android.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun DialogNegativeButton(
    text: String = "Cancel",
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onDismiss() },
        color = ListenBrainzTheme.colorScheme.dialogNegativeButton,
        shape = ListenBrainzTheme.shapes.dialogs
    ) {
        Text(
            modifier = Modifier.padding(ListenBrainzTheme.paddings.insideButton),
            text = text,
            color = ListenBrainzTheme.colorScheme.dialogNegativeButtonText,
            style = ListenBrainzTheme.textStyles.dialogButtonText
        )
    }
}

@Preview
@Composable
private fun DialogNegativeButtonPreview(){
    ListenBrainzTheme {
        DialogNegativeButton {}
    }
}