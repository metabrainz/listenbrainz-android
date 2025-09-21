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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.githubupdates.GithubUpdatesListItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel

@Composable
fun AppUpdateDialog(
    viewModel: AppUpdatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val onDismiss = { viewModel.dismissUpdateDialog() }

    if (uiState.isUpdateAvailable &&
        (uiState.latestStableRelease != null || uiState.latestRelease != null)
    ) {

        LaunchedEffect(Unit) {
            viewModel.userPromptedForUpdate()
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            AppUpdateDialogLayout(
                modifier = Modifier.padding(16.dp),
                latestStableRelease = uiState.latestStableRelease,
                latestRelease = uiState.latestRelease,
                onMaybeLater = onDismiss,
                onDownloadUpdate = {
                    onDismiss()
                    viewModel.downloadGithubUpdate(it, {}, {})
                }
            )
        }
    }
}

@Composable
fun AppUpdateDialogLayout(
    modifier: Modifier = Modifier,
    latestStableRelease: GithubUpdatesListItem?,
    latestRelease: GithubUpdatesListItem?,
    onMaybeLater: () -> Unit,
    onDownloadUpdate: (GithubUpdatesListItem) -> Unit
) {
    val isDarkTheme = onScreenUiModeIsDark()
    var selectedVersion by remember {
        mutableStateOf(if (latestStableRelease != null) "stable" else "beta")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            Text(
                text = "App update available!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = ListenBrainzTheme.colorScheme.text,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = stringResource(R.string.github_app_update),
                style = MaterialTheme.typography.bodyMedium,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )

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
                    text = stringResource(R.string.app_update_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                latestStableRelease?.let { stable ->
                    UpdateOptionItem(
                        version = stable.tagName ?: "Unknown",
                        subtitle = "Stable update",
                        isSelected = selectedVersion == "stable",
                        onSelect = { selectedVersion = "stable" }
                    )
                }

                if (latestStableRelease != null && latestRelease != null && latestStableRelease.tagName != latestRelease.tagName) {
                    HorizontalDivider()
                    Text(
                        text = "A preview version of the app is also available to download.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                latestRelease?.let { beta ->
                    if (beta.tagName != latestStableRelease?.tagName) {
                        UpdateOptionItem(
                            version = beta.tagName ?: "Unknown",
                            subtitle = "Beta update",
                            extraInfo = "This is a preview version of release.",
                            isSelected = selectedVersion == "beta",
                            onSelect = { selectedVersion = "beta" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
                    onClick = {
                        val version = if (selectedVersion == "stable") latestStableRelease
                        else latestRelease
                        if (version != null) {
                            onDownloadUpdate(
                                version
                            )
                        }
                    },
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
private fun UpdateOptionItem(
    version: String,
    subtitle: String,
    extraInfo: String? = null,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = version,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = ListenBrainzTheme.colorScheme.text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
            )
            extraInfo?.let { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = if (onScreenUiModeIsDark()) lb_purple_night else lb_purple
            )
        )
    }
}

@PreviewLightDark
@Composable
fun AppUpdateDialogPreviewLight() {
    ListenBrainzTheme {
        AppUpdateDialogLayout(
            latestStableRelease = GithubUpdatesListItem(tagName = "v2.9.0"),
            latestRelease = GithubUpdatesListItem(tagName = "v2.9.1-beta"),
            onMaybeLater = {},
            onDownloadUpdate = {}
        )
    }
}