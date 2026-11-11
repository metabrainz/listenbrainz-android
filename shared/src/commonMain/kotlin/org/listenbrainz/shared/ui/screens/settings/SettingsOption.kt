package org.listenbrainz.shared.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.link_to
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.shared.ui.components.SwitchLB
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme

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
        SettingsText(
            modifier = Modifier.weight(1f),
            title = title,
            subtitle = subtitle,
            enabled = enabled
        )

        SwitchLB(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun <T> SettingsDropdownOption(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(start = ListenBrainzTheme.paddings.settings),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsText(
            modifier = Modifier.weight(1f),
            title = title,
            subtitle = subtitle,
            enabled = true
        )

        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.animateContentSize(),
                    text = optionLabel(selected),
                    color = ListenBrainzTheme.colorScheme.hint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = ListenBrainzTheme.colorScheme.hint
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ListenBrainzTheme.colorScheme.background)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = optionLabel(option)) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
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
    enabled: Boolean = true,
    textColor: Color = ListenBrainzTheme.colorScheme.text
) {
    SettingsText(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ListenBrainzTheme.paddings.settings),
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        textColor = textColor,
    )
}

@Composable
fun SettingsHyperlink(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    textColor: Color = ListenBrainzTheme.colorScheme.text,
    iconColor: Color = ListenBrainzTheme.colorScheme.hint
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = ListenBrainzTheme.paddings.settings),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsText(
            modifier = Modifier,
            title = title,
            subtitle = subtitle,
            enabled = enabled,
            textColor = textColor,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(Res.drawable.link_to),
            contentDescription = "Arrow",
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
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

@Composable
fun SettingsLoadingOption(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ListenBrainzTheme.paddings.settings)
            .clickable(enabled = !isLoading) {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            SettingsText(
                title = title,
                subtitle = subtitle,
                enabled = !isLoading
            )
        }
        if(isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ListenBrainzTheme.colorScheme.lbSignature,
                strokeWidth = 2.dp
            )
        }
    }
}
