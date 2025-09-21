package org.listenbrainz.android.ui.screens.appupdates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel

@Composable
fun PlayStoreUpdateReadyDialog(
    viewModel: AppUpdatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isPlayStoreUpdateReadyToInstall) {
        Dialog(
            onDismissRequest = { viewModel.dismissPlayStoreUpdateDialog() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            PlayStoreUpdateReadyDialogLayout(
                error = uiState.playStoreUpdateError,
                onInstallLater = { viewModel.dismissPlayStoreUpdateDialog() },
                onInstallNow = {
                    viewModel.completePlayStoreFlexibleUpdate(context as androidx.activity.ComponentActivity)
                },
                onDismissError = { viewModel.dismissPlayStoreUpdateError() }
            )
        }
    }
}

@Composable
fun PlayStoreUpdateReadyDialogLayout(
    error: String?,
    onInstallLater: () -> Unit,
    onInstallNow: () -> Unit,
    onDismissError: () -> Unit
) {
    val isDarkTheme = onScreenUiModeIsDark()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ListenBrainzTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (error != null) {
                // Error state
                Text(
                    text = "Update Installation Failed",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "There was an error installing the update: $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onDismissError,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        color = if (isDarkTheme) lb_purple_night else lb_purple
                    )
                }
            } else {
                // Success state - ready to install
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Update Ready",
                        tint = if (isDarkTheme) lb_purple_night else lb_purple,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Update Ready to Install",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = ListenBrainzTheme.colorScheme.text
                    )
                }

                Text(
                    text = "The update has been downloaded successfully and is ready to be installed. The app will restart to complete the installation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                )

                // Info section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The app will close and reopen with the new version. Any unsaved progress may be lost.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onInstallLater,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Install Later",
                            color = if (isDarkTheme) lb_purple_night else lb_purple
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(
                        onClick = onInstallNow,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Install Now",
                            color = if (isDarkTheme) lb_purple_night else lb_purple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayStoreUpdateReadyDialogPreview() {
    ListenBrainzTheme {
        PlayStoreUpdateReadyDialogLayout(
            error = null,
            onInstallLater = {},
            onInstallNow = {},
            onDismissError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayStoreUpdateReadyDialogErrorPreview() {
    ListenBrainzTheme {
        PlayStoreUpdateReadyDialogLayout(
            error = "Failed to install update",
            onInstallLater = {},
            onInstallNow = {},
            onDismissError = {}
        )
    }
}
