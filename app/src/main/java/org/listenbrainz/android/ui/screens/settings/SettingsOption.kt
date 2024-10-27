package org.listenbrainz.android.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun SettingsSwitchOption(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ListenBrainzTheme.paddings.settings),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            SettingsText(
                title = title,
                subtitle = subtitle,
                enabled = enabled
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun SettingsHeader(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier.padding(horizontal = ListenBrainzTheme.paddings.settings),
        text = title,
        color = ListenBrainzTheme.colorScheme.lbSignature,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun SettingsTextOption(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    SettingsText(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ListenBrainzTheme.paddings.settings),
        title = title,
        subtitle = subtitle,
        enabled = enabled
    )
}

@Composable
private fun SettingsText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    enabled: Boolean,
    textColor: Color = ListenBrainzTheme.colorScheme.text,
) {
    Column(modifier) {
        Text(
            text = title,
            color = if (enabled) {
                textColor
            } else {
                ListenBrainzTheme.colorScheme.hint
            }
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                lineHeight = 18.sp,
                fontSize = 12.sp,
                color = ListenBrainzTheme.colorScheme.hint,
                modifier = Modifier
                    .padding(top = 6.dp, end = 6.dp)
                    .fillMaxWidth(0.9f)
            )
        }
    }
}