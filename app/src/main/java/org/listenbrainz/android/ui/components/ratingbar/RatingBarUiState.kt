package org.listenbrainz.android.ui.components.ratingbar


import androidx.compose.ui.graphics.Color

sealed interface StepSize {
    object ONE : StepSize
    object HALF : StepSize
}

sealed class RatingBarStyle(open val activeColor: Color) {
    companion object {
        val Default = Stroke()
    }

    class Fill(
        override val activeColor: Color = Color.Yellow,
        val inActiveColor: Color = activeColor.copy(alpha = 0.4f),
    ) : RatingBarStyle(activeColor)

    class Stroke(
        val width: Float = 1f,
        override val activeColor: Color = Color.Yellow,
        val strokeColor: Color = Color.Gray,
    ) : RatingBarStyle(activeColor)
}
