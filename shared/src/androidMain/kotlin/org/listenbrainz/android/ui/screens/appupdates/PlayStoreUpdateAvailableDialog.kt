package org.listenbrainz.android.ui.screens.appupdates

import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel

@Composable
fun PlayStoreUpdateAvailableDialog(
    viewModel: AppUpdatesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isPlayStoreFlexibleUpdateVisible && !uiState.isPlayStoreUpdateDownloading && !uiState.isPlayStoreUpdateReadyToInstall) {
        LaunchedEffect(Unit) {
            viewModel.userPromptedForUpdate()
        }

        Dialog(
            onDismissRequest = { viewModel.dismissPlayStoreUpdateDialog() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            PlayStoreUpdateAvailableDialogLayout(
                onMaybeLater = { viewModel.dismissPlayStoreUpdateDialog() },
                onDownloadUpdate = { viewModel.startPlayStoreFlexibleUpdate(context as ComponentActivity) }
            )
        }
    }
}

@Composable
fun PlayStoreUpdateAvailableDialogLayout(
    onMaybeLater: () -> Unit,
    onDownloadUpdate: () -> Unit
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
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Update Available",
                    tint = if (isDarkTheme) lb_purple_night else lb_purple,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Play Store Update Available",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = ListenBrainzTheme.colorScheme.text
                )
            }

            Text(
                text = "A new version of the app is available on Google Play Store. The update will be downloaded in the background and you can install it when ready.",
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
                    text = "This is a flexible update - you can continue using the app while it downloads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onMaybeLater,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Maybe Later",
                        color = if (isDarkTheme) lb_purple_night else lb_purple
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
                    onClick = onDownloadUpdate,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Download Update",
                        color = if (isDarkTheme) lb_purple_night else lb_purple,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PlayStoreUpdateDownloadingDialog(
    viewModel: AppUpdatesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isPlayStoreUpdateDownloading) {
        Dialog(
            onDismissRequest = { /* Don't allow dismissing during download */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            PlayStoreUpdateDownloadingDialogLayout(
                progress = uiState.playStoreUpdateDownloadProgress,
                error = uiState.playStoreUpdateError,
                onCancel = { viewModel.dismissPlayStoreUpdateDialog() }
            )
        }
    }
}

@Composable
fun PlayStoreUpdateDownloadingDialogLayout(
    progress: Int,
    error: String?,
    onCancel: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Downloading Update",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = ListenBrainzTheme.colorScheme.text
            )

            if (error != null) {
                Text(
                    text = "Error: $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        color = if (isDarkTheme) lb_purple_night else lb_purple
                    )
                }
            } else {
                // Progress indicator
                if (progress > 0) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isDarkTheme) lb_purple_night else lb_purple,
                    )

                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                } else {
                    CircularProgressIndicator(
                        color = if (isDarkTheme) lb_purple_night else lb_purple,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Preparing download...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "The update is being downloaded in the background. You can continue using the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayStoreUpdateAvailableDialogPreview() {
    ListenBrainzTheme {
        PlayStoreUpdateAvailableDialogLayout(
            onMaybeLater = {},
            onDownloadUpdate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayStoreUpdateDownloadingDialogPreview() {
    ListenBrainzTheme {
        PlayStoreUpdateDownloadingDialogLayout(
            progress = 45,
            error = null,
            onCancel = {}
        )
    }
}
