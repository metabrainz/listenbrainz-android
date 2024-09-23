package org.listenbrainz.android.ui.components.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun DialogTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    placeholder: String,
    keyboardActions: KeyboardActions = KeyboardActions.Default
){
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        textStyle = ListenBrainzTheme.textStyles.dialogTextField,
        shape = ListenBrainzTheme.shapes.dialogs,
        placeholder ={
            Text(
                text = placeholder,
                style = ListenBrainzTheme.textStyles.dialogTextField,
                color = ListenBrainzTheme.colorScheme.hint
            )
        },
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ListenBrainzTheme.colorScheme.background,
            unfocusedContainerColor = ListenBrainzTheme.colorScheme.background,
            cursorColor = ListenBrainzTheme.colorScheme.lbSignature,
            unfocusedIndicatorColor = ListenBrainzTheme.colorScheme.hint,
            focusedIndicatorColor = ListenBrainzTheme.colorScheme.lbSignature
        ),
        keyboardActions = keyboardActions
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DialogTextFieldPreview(){
    ListenBrainzTheme {
        DialogTextField(value = "", onValueChange = {}, placeholder = "Type here")
    }
}