package org.listenbrainz.shared.util

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.CoroutineScope
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme

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
    crossinline other: @Composable Modifier.() -> Modifier,
) = composed { if (condition) other() else this }

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

@Composable
fun ProvideOnLBSignatureColors(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalContentColor provides ListenBrainzTheme.colorScheme.onLbSignature,
        LocalTextStyle provides LocalTextStyle.current.merge(color = ListenBrainzTheme.colorScheme.onLbSignature),
        androidx.compose.material.LocalTextStyle provides androidx.compose.material.LocalTextStyle.current.merge(color = ListenBrainzTheme.colorScheme.onLbSignature),
        content = content,
    )
}

@Composable
fun ProvideLBButtonStyle(content: @Composable () -> Unit) {
    ProvideOnLBSignatureColors {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.merge(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            androidx.compose.material.LocalTextStyle provides androidx.compose.material.LocalTextStyle.current.merge(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            content = content
        )
    }
}

fun List<Placeable>.measureSize(): IntSize {
    var width = 0
    var height = 0
    forEach {
        width += it.width
        height += it.height
    }

    return IntSize(width, height)
}

@Composable
fun LaunchedEffectUnit(block: suspend CoroutineScope.() -> Unit) = LaunchedEffect(Unit, block)

@Composable
fun LaunchedEffectUnitMainThread(block: () -> Unit) = DisposableEffect(Unit) {
    block()
    EmptyDisposableEffectResult
}

@Composable
fun LaunchedEffectMainThread(vararg keys: Any?, block: () -> Unit) = DisposableEffect(*keys) {
    block()
    EmptyDisposableEffectResult
}

@Composable
fun LaunchedEffectMainThread(key1: Any?, block: () -> Unit) = DisposableEffect(key1) {
    block()
    EmptyDisposableEffectResult
}

private val EmptyDisposableEffectResult = object : DisposableEffectResult {
    override fun dispose() = Unit
}

@Composable
fun getStatusBarHeight() =
    WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()

@Composable
fun getNavigationBarHeight() =
    WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()

@Composable
fun HorizontalSpacer(width: Dp) = Spacer(Modifier.width(width))

@Composable
fun VerticalSpacer(height: Dp) = Spacer(Modifier.height(height))

@Composable
fun ColumnScope.Spacer(height: Dp) = Spacer(modifier = Modifier.height(height))

@Composable
fun RowScope.Spacer(height: Dp) = Spacer(modifier = Modifier.width(height))

@Composable
fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }
@Composable
fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

@Composable
fun Int.toSp() = with(LocalDensity.current) { this@toSp.toSp() }
@Composable
fun Float.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun TextUnit.toDp() = with(LocalDensity.current) { this@toDp.toDp() }
