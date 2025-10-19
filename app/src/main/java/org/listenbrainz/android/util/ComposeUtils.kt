package org.listenbrainz.android.util

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter

@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.optionalSharedElement(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    key: Any,
) = composed {
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            sharedElement(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        this
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.optionalSharedBounds(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    key: Any,
) = composed {
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            sharedBounds(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else {
        this
    }
}

fun <K, V> snapshotStateMapSaver() = Saver<SnapshotStateMap<K, V>, Any>(
    save = { stateMap ->
        stateMap.toList()
    },
    restore = { value ->
        @Suppress("UNCHECKED_CAST")
        val list = value as? List<Pair<K, V>>
        if (list != null) {
            mutableStateMapOf<K, V>().apply { putAll(list) }
        } else {
            mutableStateMapOf()
        }
    },
)

inline fun Modifier.thenIf(
    condition: Boolean,
    crossinline other: Modifier.() -> Modifier,
) = if (condition) other() else this

fun <T : Any> PagingData<T>.snapshot(): List<T> {
    val pagingDataPresenter = object : PagingDataPresenter<T>(
        cachedPagingData = this,
    ) {
        override suspend fun presentPagingDataEvent(
            event: PagingDataEvent<T>,
        ) = Unit
    }

    return pagingDataPresenter.snapshot().items
}

fun Modifier.consumeHorizontalDrag(enabled: Boolean = true) = composed {
    draggable(
        state = rememberDraggableState {},
        orientation = Orientation.Horizontal,
        enabled = enabled
    )
}

fun Modifier.consumeVerticalDrag(enabled: Boolean = true) = composed {
    draggable(
        state = rememberDraggableState {},
        orientation = Orientation.Vertical,
        enabled = enabled
    )
}