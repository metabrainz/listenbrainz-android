package org.listenbrainz.shared.util


import org.listenbrainz.shared.ui.components.ratingbar.StepSize
import kotlin.math.ceil
import kotlin.math.round

fun calculateRatingStars(
    touchX: Float,
    stars: Int,
    starPx: Float,
    spacePx: Float,
    stepSize: StepSize
): Float {
    val totalStarPx = starPx + spacePx
    var value = (touchX / totalStarPx).coerceIn(0f, stars.toFloat())

    value = when (stepSize) {
        StepSize.ONE -> ceil(value)
        StepSize.HALF -> round(value * 2) / 2f
    }

    return value
}
