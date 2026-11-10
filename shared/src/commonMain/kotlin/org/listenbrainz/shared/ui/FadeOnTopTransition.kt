package org.listenbrainz.shared.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

private const val FADE_DURATION_MS = 200

/**
 * Entry metadata that fades the entry in over the screen below it, and back out when popped.
 * A predictive back gesture drives the fade out with its progress.
 */
val fadeOnTopTransition: Map<String, Any> =
    NavDisplay.transitionSpec {
        fadeIn(tween(FADE_DURATION_MS)) togetherWith ExitTransition.KeepUntilTransitionsFinished
    } + NavDisplay.popTransitionSpec {
        EnterTransition.None togetherWith fadeOut(tween(FADE_DURATION_MS))
    } + NavDisplay.predictivePopTransitionSpec {
        EnterTransition.None togetherWith fadeOut(tween(FADE_DURATION_MS))
    }
