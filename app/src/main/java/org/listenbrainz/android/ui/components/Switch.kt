package org.listenbrainz.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun Switch(modifier: Modifier = Modifier, checked: Boolean, onCheckedChange: (Boolean) -> Unit)
{
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { onCheckedChange(it) },
        colors  = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF918EB0),
            checkedTrackColor = Color(0xFF353070),
            uncheckedThumbColor = Color(0xFFD9D9D9),
            uncheckedTrackColor= Color(0xFF8D8D8D),
            checkedTrackAlpha = 1f,
            uncheckedTrackAlpha = 1f,
        ),
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSwitch() {
    val checkedState = remember { mutableStateOf(true) }
    org.listenbrainz.android.ui.components.Switch(
        checked = checkedState.value,
        onCheckedChange = { checkedState.value = it }
    )
}
