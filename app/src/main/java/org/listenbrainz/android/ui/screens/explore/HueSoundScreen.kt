package org.listenbrainz.android.ui.screens.explore


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.viewmodel.HueSoundViwModel
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

//TODO : Make the screen adaptive
@Composable
fun HueSoundScreen(modifier: Modifier = Modifier) {
    val viewModel: HueSoundViwModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.value) {
        Log.d("HueSoundScreen ${uiState.value.releases}")
    }
    val lazyGridState = rememberLazyGridState()
    var isScrollingDown by remember { mutableStateOf(false) }

    LaunchedEffect(lazyGridState.firstVisibleItemScrollOffset) {
        val firstVisibleIndex = lazyGridState.firstVisibleItemIndex
        val scrollOffset = lazyGridState.firstVisibleItemScrollOffset

        // Detect if scrolling down or up
        isScrollingDown = firstVisibleIndex > 0 || scrollOffset > 50
    }

    val wheelOffset by animateDpAsState(
        targetValue = if (isScrollingDown) 200.dp else 0.dp, // Moves down when scrolling
        animationSpec = tween(durationMillis = 800),
        label = "Wheel Offset Animation"
    )
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(100.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(items = uiState.value.releases, key = { it.releaseId }) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://archive.org/download/mbid-${it.releaseId}/mbid-${it.releaseId}-${it.caaId}_thumb250.jpg")
                    .placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                    .error(R.drawable.ic_coverartarchive_logo_no_text)
                    .size(100, 100)
                    .crossfade(true)
                    .build(),
                contentDescription = it.caaId,
                placeholder = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .aspectRatio(1f)
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = wheelOffset) // Apply animation
    ) {
        Spacer(modifier.weight(1f))
        SmoothIndependentColorWheels {
            Log.d(it.toHex())
            viewModel.onColorPickled(it.toHex())
        }
    }

}


fun Color.toHex(): String {
    val argb = this.toArgb()
    return String.format("%06X", (argb and 0xFFFFFF))
}

@Composable
fun SmoothIndependentColorWheels(
    modifier: Modifier = Modifier,
    outerWheelColors: List<Color> = defaultColorList(),
    innerWheelWidth: Float = 0.5f,
    onColorSelected: (Color) -> Unit
) {
    var outerWheelRotation by remember { mutableStateOf(0f) }
    var innerWheelRotation by remember { mutableStateOf(0f) }

    val animatedOuterRotation by animateFloatAsState(
        targetValue = outerWheelRotation,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 50f
        ),
        label = "outerWheelRotation"
    )

    val animatedInnerRotation by animateFloatAsState(
        targetValue = innerWheelRotation,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 50f
        ),
        label = "innerWheelRotation"
    )

    var interactingWheel by remember { mutableStateOf(WheelType.NONE) }
    var previousTouchAngle by remember { mutableStateOf(0f) }

    var selectedOuterColorIndex by remember { mutableStateOf(0) }
    var selectedShadeIndex by remember { mutableStateOf(5) }

    val colorShades = remember(selectedOuterColorIndex) {
        generateShades(outerWheelColors[selectedOuterColorIndex], 12)
    }

    LaunchedEffect(colorShades, selectedShadeIndex) {
        onColorSelected(colorShades[selectedShadeIndex])
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RectangleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) {
                    (LocalConfiguration.current.screenWidthDp / 2).dp
                })
                .align(Alignment.BottomCenter)
                .clip(RectangleShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val center = Offset(size.width / 2f, size.height.toFloat())
                                val distanceFromCenter = offset.distanceTo(center)

                                // Calculate radius of inner wheel
                                val canvasRadius = minOf(size.width, size.height) * 0.9f
                                val innerRadius = canvasRadius * innerWheelWidth

                                // Determine which wheel is being interacted with
                                interactingWheel = if (distanceFromCenter <= innerRadius) {
                                    WheelType.INNER
                                } else if (distanceFromCenter <= canvasRadius) {
                                    WheelType.OUTER
                                } else {
                                    WheelType.NONE
                                }

                                // Store the initial touch angle
                                previousTouchAngle = calculateAngle(center, offset)
                            },
                            onDragEnd = {
                                interactingWheel = WheelType.NONE
                            },
                            onDragCancel = {
                                interactingWheel = WheelType.NONE
                            },
                            onDrag = { change, _ ->
                                val center = Offset(size.width / 2f, size.height.toFloat())
                                val currentAngle = calculateAngle(center, change.position)
                                var angleDiff = currentAngle - previousTouchAngle

                                // Handle angle wrapping around 360 degrees
                                if (angleDiff > 180) angleDiff -= 360
                                if (angleDiff < -180) angleDiff += 360

                                // Apply rotation to the appropriate wheel
                                when (interactingWheel) {
                                    WheelType.OUTER -> {
                                        outerWheelRotation += angleDiff
                                    }

                                    WheelType.INNER -> {
                                        innerWheelRotation += angleDiff
                                    }

                                    else -> {
                                        //Do nothing
                                    }
                                }

                                previousTouchAngle = currentAngle
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height.toFloat())
                            val distanceFromCenter = offset.distanceTo(center)
                            val canvasRadius = minOf(size.width, size.height) * 0.9f
                            val innerRadius = canvasRadius * innerWheelWidth

                            val rawAngle = calculateAngle(center, offset)

                            val adjustedAngle = when {
                                distanceFromCenter <= innerRadius -> {
                                    (rawAngle - animatedInnerRotation + 360) % 360
                                }

                                distanceFromCenter <= canvasRadius -> {
                                    (rawAngle - animatedOuterRotation + 360) % 360
                                }

                                else -> rawAngle
                            }
                            if (distanceFromCenter <= innerRadius) {
                                val index =
                                    ((adjustedAngle / (360f / colorShades.size)).toInt() % colorShades.size)
                                selectedShadeIndex = index
                            } else if (distanceFromCenter <= canvasRadius) {
                                val index =
                                    ((adjustedAngle / (360f / outerWheelColors.size)).toInt() % outerWheelColors.size)
                                Log.d("Outer wheel tap $index")
                                selectedOuterColorIndex = index
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height)
                val radius = minOf(size.width, size.height) * 0.9f
                val innerRadius = radius * innerWheelWidth

                rotate(animatedOuterRotation, center) {
                    val outerSegmentAngle = 360f / outerWheelColors.size
                    outerWheelColors.forEachIndexed { index, color ->
                        val startAngle = index * outerSegmentAngle
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = outerSegmentAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            alpha = 1f
                        )
                    }
                }

                rotate(animatedInnerRotation, center) {
                    val innerSegmentAngle = 360f / colorShades.size
                    colorShades.forEachIndexed { index, color ->
                        val startAngle = index * innerSegmentAngle
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = innerSegmentAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                            size = Size(innerRadius * 2, innerRadius * 2),
                            alpha = 1f
                        )
                    }
                }

                // Draw center divider circle (not rotated)
                drawCircle(
                    color = Color.White,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

// Define an enum for wheel type
enum class WheelType {
    OUTER, INNER, NONE
}

private fun calculateAngle(center: Offset, point: Offset): Float {
    val angleRad = atan2(point.y - center.y, point.x - center.x)
    return (Math.toDegrees(angleRad.toDouble()).toFloat() + 360) % 360
}

fun defaultColorList(): List<Color> {
    return listOf(
        Color.Red,
        Color(0xFFFF3366), // Pink
        Color(0xFFFF00FF), // Magenta
        Color(0xFF9900FF), // Purple
        Color(0xFF3300FF), // Deep Blue
        Color(0xFF0033FF), // Blue
        Color(0xFF0099FF), // Light Blue
        Color(0xFF00CCFF), // Cyan
        Color(0xFF00FFFF), // Aqua
        Color(0xFF00FFCC), // Light Teal
        Color(0xFF00FF66), // Light Green
        Color(0xFF00FF00), // Green
        Color(0xFF66FF00), // Lime
        Color(0xFFCCFF00), // Light Yellow-Green
        Color(0xFFFFFF00), // Yellow
        Color(0xFFFFCC00), // Light Orange
        Color(0xFFFF9900), // Orange
        Color(0xFFFF6600), // Dark Orange
        Color(0xFFFF3300)  // Red-Orange
    )
}

fun generateShades(baseColor: Color, count: Int): List<Color> {
    val shades = mutableListOf<Color>()

    // Get HSL values
    val hsl = convertRgbToHsl(baseColor.red, baseColor.green, baseColor.blue)
    val (h, s, l) = hsl

    // Create shades by varying the lightness
    for (i in 0 until count) {
        // Calculate lightness from 0.95 (very light) to 0.05 (very dark)
        val lightness = 0.95f - i * (0.90f / (count - 1))
        val color = convertHslToRgb(h, s, lightness)
        shades.add(color)
    }

    return shades
}

fun convertRgbToHsl(r: Float, g: Float, b: Float): Triple<Float, Float, Float> {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = 0f
    var s = 0f
    val l = (max + min) / 2

    if (delta != 0f) {
        s = if (l < 0.5f) delta / (max + min) else delta / (2 - max - min)

        h = when {
            r == max -> (g - b) / delta + (if (g < b) 6 else 0)
            g == max -> (b - r) / delta + 2
            else -> (r - g) / delta + 4
        }

        h /= 6
    }

    return Triple(h, s, l)
}

fun convertHslToRgb(h: Float, s: Float, l: Float): Color {
    if (s == 0f) {
        return Color(l, l, l)
    }

    val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
    val p = 2 * l - q

    val r = hueToRgb(p, q, h + 1f / 3f)
    val g = hueToRgb(p, q, h)
    val b = hueToRgb(p, q, h - 1f / 3f)

    return Color(r, g, b)
}

fun hueToRgb(p: Float, q: Float, t: Float): Float {
    var tt = t
    if (tt < 0f) tt += 1f
    if (tt > 1f) tt -= 1f

    return when {
        tt < 1f / 6f -> p + (q - p) * 6f * tt
        tt < 1f / 2f -> q
        tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
        else -> p
    }
}

fun Offset.distanceTo(other: Offset): Float {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}

@Preview
@Composable
private fun CircularWheelPreview() {
    SmoothIndependentColorWheels {}
}