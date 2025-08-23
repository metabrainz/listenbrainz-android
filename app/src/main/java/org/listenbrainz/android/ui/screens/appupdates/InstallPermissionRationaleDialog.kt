package org.listenbrainz.android.ui.screens.appupdates

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
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
fun InstallPermissionRationaleDialog(
    viewModel: AppUpdatesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isInstallPermissionRationaleVisible) {
        Dialog(
            onDismissRequest = { viewModel.hideInstallPermissionRationale() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            InstallPermissionRationaleDialogLayout(
                onGrantPermission = {
                    viewModel.hideInstallPermissionRationale()
                    // Open install permission settings
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                onDismiss = {
                    viewModel.hideInstallPermissionRationale()
                }
            )
        }
    }
}

@Composable
fun InstallPermissionRationaleDialogLayout(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
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
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = if (isDarkTheme) lb_purple_night else lb_purple,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Install Permission Required",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = ListenBrainzTheme.colorScheme.text,
                textAlign = TextAlign.Center
            )

            Text(
                text = "To install app updates from outside the Play Store, we need permission to install unknown apps.",
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
                        text = "How to grant permission:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = ListenBrainzTheme.colorScheme.text
                    )
                    Text(
                        text = "1. Tap 'Grant Permission' below",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "2. Toggle 'Allow from this source' ON",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "3. Return to the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onGrantPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Grant Permission",
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) lb_purple_night else lb_purple
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InstallPermissionRationaleDialogPreview() {
    ListenBrainzTheme {
        InstallPermissionRationaleDialogLayout(
            onGrantPermission = {},
            onDismiss = {}
        )
    }
}
