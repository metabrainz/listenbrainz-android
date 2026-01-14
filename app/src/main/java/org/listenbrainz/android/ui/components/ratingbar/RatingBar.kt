package org.listenbrainz.android.ui.components.ratingbar

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.calculateRatingStars


@Composable
fun RatingBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 32.dp,
    spaceBetween: Dp = 6.dp,
    stepSize: StepSize = StepSize.ONE,
    isIndicator: Boolean = false,
    style: RatingBarStyle = RatingBarStyle.Default,
) {
    val density = LocalDensity.current
    val starPx = with(density) { size.toPx() }
    val spacePx = with(density) { spaceBetween.toPx() }

    var lastValue by remember { mutableStateOf(value) }

    Row(modifier = modifier
        .pointerInput(isIndicator) {
            if (isIndicator) return@pointerInput
            detectDragGestures(
                onDragEnd = {
                    if (!isIndicator) onRatingChanged(lastValue)
                }) { change, _ ->
                val x = change.position.x.coerceAtLeast(0f)
                val newRating = calculateRatingStars(x, numOfStars, starPx, spacePx, stepSize)
                lastValue = newRating
                onValueChange(newRating)
            }
        }
        .pointerInput(isIndicator) {
            if (isIndicator) return@pointerInput
            detectTapGestures { offset ->
                val newRating =
                    calculateRatingStars(offset.x, numOfStars, starPx, spacePx, stepSize)
                lastValue = newRating
                onValueChange(newRating)
                onRatingChanged(newRating)
            }
        }) {
        var remaining = value
        repeat(numOfStars) { index ->
            val fraction = when {
                remaining >= 1f -> {
                    remaining -= 1f; 1f
                }

                remaining > 0f -> {
                    val f = remaining; remaining = 0f; f
                }

                else -> 0f
            }

            RatingStar(fraction, size, style)

            if (index < numOfStars - 1) Spacer(modifier = Modifier.width(spaceBetween))
        }
    }
}


@Composable
private fun RatingStar(fraction: Float, size: Dp, style: RatingBarStyle) {
    Box(modifier = Modifier.size(size)) {
        when (style) {
            is RatingBarStyle.Fill -> {
                StarIcon(style.inActiveColor)
                StarFillClip(fraction) { StarIcon(style.activeColor) }
            }

            is RatingBarStyle.Stroke -> {
                StarOutline(style.strokeColor)
                StarFillClip(fraction) { StarIcon(style.activeColor) }
            }
        }
    }
}

@Composable
private fun StarFillClip(fraction: Float, content: @Composable () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
            clip = true
            shape = RectangleShape
        }
        .drawWithContent {
            clipRect(right = size.width * fraction) {
                this@drawWithContent.drawContent()
            }
        }) {
        content()
    }
}

@Composable
private fun StarIcon(color: Color) = Icon(
    Icons.Filled.Star, contentDescription = null, tint = color, modifier = Modifier.fillMaxSize()
)

@Composable
private fun StarOutline(color: Color) = Icon(
    Icons.Outlined.StarBorder,
    contentDescription = null,
    tint = color,
    modifier = Modifier.fillMaxSize()
)


@Preview
@Composable
fun CustomRatingBarPreview() {
    PreviewSurface {
        var rating by rememberSaveable {
            mutableStateOf(2.5f)
        }
        RatingBar(
            value = 2.5f, size = 16.dp,
            style = RatingBarStyle.Fill(
                inActiveColor = Color.Transparent,
                activeColor = ListenBrainzTheme.colorScheme.golden
            ),
            spaceBetween = 1.5.dp,
            onValueChange = {},
            onRatingChanged = {})

    }
}
