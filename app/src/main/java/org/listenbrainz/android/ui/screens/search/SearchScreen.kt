package org.listenbrainz.android.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.search.SearchUiState
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    queryValue: TextFieldValue,
    onDismiss: () -> Unit,
    onQueryChange: (TextFieldValue) -> Unit,
    onClear: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSearch: (String) -> Unit = {
        keyboardController?.hide()
    },
    placeholderText: String,
    onErrorShown: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    focusManager: FocusManager = LocalFocusManager.current,
    window: WindowInfo = LocalWindowInfo.current,
    content: @Composable () -> Unit
) {
    // Used for initial window focus.
    LaunchedEffect(window) {
        snapshotFlow { window.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TextField(
            value = queryValue,
            modifier = Modifier.focusRequester(focusRequester).fillMaxWidth().padding(vertical = 3.dp),
            onValueChange = {
                onQueryChange(it)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            keyboardController?.hide()
                            onDismiss()
                        },
                    contentDescription = "Go Back",
                    tint = ListenBrainzTheme.colorScheme.hint
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Cancel,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onClear()
                            keyboardController?.show()
                        },
                    contentDescription = "Close Search",
                    tint = ListenBrainzTheme.colorScheme.hint
                )
            },
            placeholder = {
                Text(text = placeholderText, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ListenBrainzTheme.colorScheme.background,
                unfocusedContainerColor = ListenBrainzTheme.colorScheme.background,
                focusedTextColor = ListenBrainzTheme.colorScheme.text,
                unfocusedTextColor = ListenBrainzTheme.colorScheme.text,
                cursorColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(uiState.query) }),
            singleLine = true
        )

        HorizontalDivider(color = ListenBrainzTheme.colorScheme.text)
        Column(
            modifier = Modifier
                .pointerInput(key1 = "Keyboard") {
                    // Tap to hide keyboard.
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
        ) {

            // Error bar for showing errors
            ErrorBar(uiState.error, onErrorShown)

            // Main Content
            content()
        }
    }
}