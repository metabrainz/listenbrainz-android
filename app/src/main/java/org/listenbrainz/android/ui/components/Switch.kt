package org.listenbrainz.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.ui.graphics.Color

@Composable
fun Switch(checked: Boolean, onCheckedChange: (Boolean) -> Unit)
{
    Switch(
        checked = checked,
        onCheckedChange = { onCheckedChange(it) },
        colors  = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF918EB0),
            checkedTrackColor = Color(0xFF353070),
            uncheckedThumbColor = Color(0xFFD9D9D9),
            uncheckedTrackColor= Color(0xFF8D8D8D),
        ),
    )
}