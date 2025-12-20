package org.listenbrainz.android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun FloatingContentAwareLayout(
    modifier: Modifier = Modifier,
    buttonAlignment: Alignment,
    floatingContent: @Composable () -> Unit,
    content: @Composable (DpSize) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints: Constraints ->
        val mainPlaceables: List<Placeable> = subcompose("float") {
            floatingContent()
        }
            .map { measurable: Measurable ->
                measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }

        var height = 0
        var width = 0

        // Get height of card and any other composables.
        mainPlaceables.forEach {
            height += it.height
            width += it.width
        }

        val dependentPlaceables: List<Placeable> = subcompose("content") {
            content(DpSize((width / density).dp, (height / density).dp))
        }
            .map { measurable: Measurable ->
                measurable.measure(constraints)
            }

        var dependentHeight = 0
        var dependentWidth = 0
        dependentPlaceables.forEach {
            dependentWidth += it.width
            dependentHeight += it.height
        }

        layout(height = dependentHeight, width = dependentWidth) {
            dependentPlaceables.forEach { placeable: Placeable ->
                placeable.placeRelative(0, 0)
            }

            mainPlaceables.forEach { placeable: Placeable ->
                placeable.placeRelative(
                    buttonAlignment.align(
                        size = IntSize(placeable.width, placeable.height),
                        space = IntSize(dependentWidth, dependentHeight),
                        layoutDirection = LayoutDirection.Ltr,
                    ),
                    zIndex = 1f,
                )
            }
        }
    }
}
