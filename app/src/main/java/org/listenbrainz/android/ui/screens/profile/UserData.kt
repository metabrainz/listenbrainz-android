package org.listenbrainz.android.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.ListensViewModel

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserData(
    viewModel: ListensViewModel,
    context: Context = LocalContext.current
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isNotificationServiceAllowed by remember {
        mutableStateOf(
            viewModel.appPreferences.isNotificationServiceAllowed
        )
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationServiceAllowed =
                    viewModel.appPreferences.isNotificationServiceAllowed
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = 0.dp,
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = viewModel.appPreferences.username!!,
                    modifier = Modifier.padding(4.dp),
                    color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                var accessToken by remember {
                    mutableStateOf(
                        viewModel.appPreferences.lbAccessToken ?: ""
                    )
                }

                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { newText ->
                        accessToken = newText
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            coroutineScope.launch {
                                val tokenValid = viewModel.validateUserToken(accessToken)
                                if (tokenValid != null && tokenValid) {
                                    viewModel.appPreferences.lbAccessToken = accessToken
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                else {
                                    viewModel.appPreferences.lbAccessToken = ""
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
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                        focusedBorderColor = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                        unfocusedBorderColor = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                    )
                )
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Button(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        if (!isNotificationServiceAllowed) {
                            showDialog = true
                        }
                    }
                ) {
                    Text(
                        text = when {
                            viewModel.appPreferences.isNotificationServiceAllowed -> {
                                "Great! You've already given the app permission to your notifications"
                            }
                            else -> {
                                "Start submitting listens by giving the app permission to your notifications"
                            }
                        },
                        color = if (onScreenUiModeIsDark()) Color.White else Color.Black
                    )
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
        viewModel = hiltViewModel()
    )
}