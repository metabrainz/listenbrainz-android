package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.ui.theme.ListenBrainzTheme


@Composable
fun BaseDialog(
    onDismiss: () -> Unit,
    title: @Composable BoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    footer: @Composable BoxScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .sizeIn(
                    maxWidth = LocalConfiguration.current.screenWidthDp.dp - LocalConfiguration.current.screenWidthDp.dp / 18,
                    maxHeight = LocalConfiguration.current.screenHeightDp.dp - 24.dp
                )
                .verticalScroll(rememberScrollState()),
            shape = ListenBrainzTheme.shapes.dialogs,
            color = ListenBrainzTheme.colorScheme.background
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().padding(ListenBrainzTheme.paddings.dialogContent)) {
                    title(this)
                }
                
                Divider(color = ListenBrainzTheme.colorScheme.hint)
                
                Column(modifier = Modifier.fillMaxWidth().padding(ListenBrainzTheme.paddings.dialogContent)) {
                    content(this)
                }
                
                Divider(color = ListenBrainzTheme.colorScheme.hint)
    
                Box(modifier = Modifier.fillMaxWidth().padding(ListenBrainzTheme.paddings.dialogContent)) {
                    footer(this)
                }
            }
            
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun BaseDialogPreview(){
    ListenBrainzTheme {
        BaseDialog(
            onDismiss = {},
            title = {
                Text(text = "Title", color = ListenBrainzTheme.colorScheme.text)
            },
            content = {
                Text(text = "Content", color = ListenBrainzTheme.colorScheme.text)
                
            },
            footer = {
                Text(text = "Footer", color = ListenBrainzTheme.colorScheme.text)
            }
        )
    }
}