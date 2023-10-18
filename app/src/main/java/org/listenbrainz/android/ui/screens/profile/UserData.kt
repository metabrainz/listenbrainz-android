package org.listenbrainz.android.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserData(
    preferencesUiState: PreferencesUiState,
    updateNotificationServicePermissionStatus: () -> Unit,
    validateUserToken: suspend (String) -> Boolean,
    setToken: (String) -> Unit,
    context: Context = LocalContext.current
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LifecycleResumeEffect(Unit) {
        updateNotificationServicePermissionStatus()
        onPauseOrDispose {}
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = 0.dp,
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preferencesUiState.username,
                    modifier = Modifier.padding(4.dp),
                    color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                )
            }
            
            var tempAccessToken by remember {
                mutableStateOf(preferencesUiState.accessToken)
            }
            if(preferencesUiState.accessToken.isEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = preferencesUiState.accessToken,
                        onValueChange = { newText ->
                            tempAccessToken = newText
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                coroutineScope.launch {
                                    val tokenValid = validateUserToken(preferencesUiState.accessToken)
                                    if (tokenValid) {
                                        coroutineScope.launch {
                                            setToken(tempAccessToken)
                                        }
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    } else {
                                        coroutineScope.launch {
                                            setToken("")
                                        }
                                        Toast.makeText(
                                            context,
                                            "Invalid token",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color.Transparent),
                        label = {
                            Text(
                                "Your access token from the website",
                                color = if (onScreenUiModeIsDark()) Color.White else Color.Black
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                            unfocusedBorderColor = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                            cursorColor = if (onScreenUiModeIsDark()) Color.White else Color.Black
                        )
                    )
                }
            }

            if(!preferencesUiState.isNotificationServiceAllowed) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Button(
                        modifier = Modifier.padding(4.dp),
                        onClick = {
                            showDialog = !showDialog
                        }
                    ) {
                        Text(
                            text = "Start submitting listens by giving the app permission to your notifications",
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Grant Media Control Permissions") },
            text = { Text("The listen service requires the special Notification " +
                    "Listener Service Permission to run. Please grant this permission to" +
                    " ListenBrainz for Android if you want to use the service.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    } else {
                        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    // Your code to update preferences and switch state
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
fun UserDataPreview() {
    UserData(
        PreferencesUiState(),
        {},
        { true },
        {}
    )
}