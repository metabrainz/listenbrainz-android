package org.listenbrainz.android.ui.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

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

@Composable
fun CustomSeekBar(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    progress: Float,
    onValueChange: (Float) -> Unit,
    shape: Shape = RectangleShape,
    progressColor: Color = ListenBrainzTheme.colorScheme.lbSignature,
    remainingProgressColor: Color = ListenBrainzTheme.colorScheme.hint
) {
    val range = 0f..1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(progress, range)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.toFloat()
                        val newProgress = (offset.x / width).coerceIn(range)
                        onValueChange(newProgress)
                    },
                    onDrag = { change, _ ->
                        val width = size.width.toFloat()
                        val newProgress = (change.position.x / width).coerceIn(range)
                        onValueChange(newProgress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    val newProgress = (offset.x / width).coerceIn(range)
                    onValueChange(newProgress)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .graphicsLayer {
                    alpha = 0.2f
                }
                .background(remainingProgressColor, shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(5.dp)
                .background(progressColor, shape)
        )
    }
}
