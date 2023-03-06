package org.listenbrainz.android.ui.components

import androidx.annotation.FloatRange
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import org.listenbrainz.android.R

@Composable
fun SeekBar(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    progress: Float,
    onValueChange: (Float) -> Unit,
    onValueChanged: () -> Unit
) {
    Slider(
        modifier = modifier,
        value = progress,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChanged,
        colors = SliderDefaults.colors(
            thumbColor = colorResource(id = R.color.app_bg),
            activeTrackColor = colorResource(id = R.color.bp_color_primary)
        )

    )
}