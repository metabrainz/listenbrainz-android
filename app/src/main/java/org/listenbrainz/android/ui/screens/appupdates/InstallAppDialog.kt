package org.listenbrainz.android.ui.screens.appupdates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GetApp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel

@Composable
fun InstallAppDialog(
    viewModel: AppUpdatesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isInstallAppDialogVisible && uiState.downloadedApkUri != null) {
        Dialog(
            onDismissRequest = { viewModel.hideInstallAppDialog() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            InstallAppDialogLayout(
                onInstallNow = {
                    viewModel.installDownloadedApp()
                },
                onInstallLater = {
                    viewModel.hideInstallAppDialog()
                }
            )
        }
    }
}

@Composable
fun InstallAppDialogLayout(
    onInstallNow: () -> Unit,
    onInstallLater: () -> Unit
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GetApp,
                contentDescription = null,
                tint = if (isDarkTheme) lb_purple_night else lb_purple,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Update Downloaded",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = ListenBrainzTheme.colorScheme.text,
                textAlign = TextAlign.Center
            )

            Text(
                text = "The update has been downloaded successfully. Do you want to install it now?",
                style = MaterialTheme.typography.bodyMedium,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "What happens next:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = ListenBrainzTheme.colorScheme.text
                    )
                    Text(
                        text = "• The Android system installer will open",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• You'll be prompted to confirm the installation",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• The app will be updated to the latest version",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onInstallLater,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Install Later",
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
                    )
                }

                TextButton(
                    onClick = onInstallNow,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Install Now",
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) lb_purple_night else lb_purple
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstallAppDialogPreview() {
    ListenBrainzTheme {
        InstallAppDialogLayout(
            onInstallNow = {},
            onInstallLater = {}
        )
    }
}
