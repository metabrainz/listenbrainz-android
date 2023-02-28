package org.listenbrainz.android.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.ui.theme.ListenBrainzTheme


/** [DialogLB] : Standardised Alert Dialog for ListenBrainz.
 *
 * @param [onDismiss] What to do when dialog is dismissed
 * @param [options] Options to provide to the user. Max number of options is 2.
 * @param [title] Title of the dialog. If empty, title is removed.
 * @param [description] Body text of the dialog. If empty, description is removed.
 * @param [dismissOnBackPress] True by default.
 * @param [dismissOnClickOutside] True by default.
 * @param [enableDismissButton] Enables the button (dismiss) on bottom left of dialog. Disabled by default.
 * @param [dismissButtonText] Text of the bottom left button. "Cancel" by default.
 * @param [dismissButtonListener] Listener to call when the bottom left dismiss button is clicked. onDismiss called by default.
 * */
@Composable
fun DialogLB(
    onDismiss: () -> Unit,
    options: Array<String> = arrayOf(),
    firstOptionListener: () -> Unit = {},
    secondOptionListener: () -> Unit = {},
    title: String = "",
    description: String = "",
    enableDismissButton: Boolean = false,
    dismissButtonListener: () -> Unit = {},
    dismissButtonText: String = "Cancel",
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true
){
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            if (enableDismissButton || options.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                        if (options.size == 2) {
                            Button(
                                onClick = {
                                    secondOptionListener()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(end = 8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(text = options[1])
                            }
                        }
                        if (options.isNotEmpty()) {
                            Button(
                                onClick = {
                                    firstOptionListener()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(text = options[0])
                            }
                        }
                    }
        
                    if (enableDismissButton) {
                        Row(Modifier.align(Alignment.CenterStart)) {
                            Button(
                                onClick = {
                                    dismissButtonListener()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text(text = dismissButtonText)
                            }
                        }
                    }
                }
            }
            
        },
        title = {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        properties = DialogProperties(dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = dismissOnClickOutside)
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview(){
    ListenBrainzTheme {
        DialogLB(enableDismissButton = true, onDismiss = {})
    }
}