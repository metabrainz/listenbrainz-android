package org.listenbrainz.android.ui.screens.explore


import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.HueSoundViwModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

//TODO : Make the screen adaptive
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HueSoundScreen(
    modifier: Modifier = Modifier,
    socialViewModel: SocialViewModel = hiltViewModel(),
    hueSoundViwModel: HueSoundViwModel = hiltViewModel(),
    topBarActions: TopBarActions,
    snackbarState: SnackbarHostState,
    goToArtistPage: (String) -> Unit = {}
) {
    val uiState by hueSoundViwModel.uiState.collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridState()
    var isScrollingDown by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val wheelOffset by animateFloatAsState(
        targetValue = if (isScrollingDown) 1f else 0f,
        animationSpec = tween(1000),
        label = "Wheel Offset"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.HueSound.title
        )
        if (showBottomSheet) {
            ModalBottomSheet(
                sheetState = bottomSheetState,
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxHeight(),
                onDismissRequest = { showBottomSheet = false }) {
                LazyColumn(state = rememberLazyListState()) {
                    items(
                        items = uiState.selectedRelease.recordings,
                        key = {
                            it.trackMetadata.additionalInfo?.recordingMbid
                                ?: "${it.trackMetadata.artistName}_${it.trackMetadata.trackName}"
                        }) {
                        ListenCardSmallDefault(
                            modifier = Modifier.padding(
                                horizontal = ListenBrainzTheme.paddings.horizontal,
                                vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                            ),
                            metadata = Metadata(trackMetadata = it.trackMetadata),
                            coverArtUrl = getCoverArtUrl(
                                caaId = uiState.selectedRelease.caaId,
                                caaReleaseMbid = uiState.selectedRelease.releaseId,
                            ),
                            goToArtistPage = goToArtistPage,
                            onDropdownSuccess = { message ->
                                scope.launch {
                                    snackbarState.showSnackbar(message)
                                }
                            },
                            onDropdownError = { error ->
                                scope.launch {
                                    snackbarState.showSnackbar(error.toast)
                                }
                            },
                            onClick = { socialViewModel.playListen(it.trackMetadata) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }


        LaunchedEffect(lazyGridState.firstVisibleItemScrollOffset) {
            val firstVisibleIndex = lazyGridState.firstVisibleItemIndex
            val scrollOffset = lazyGridState.firstVisibleItemScrollOffset

            // Detect if scrolling down or up
            isScrollingDown = firstVisibleIndex > 0 || scrollOffset > 50
        }

        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(100.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(items = uiState.releases, key = { it.releaseId }) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getCoverArtUrl(caaId = it.caaId, caaReleaseMbid = it.releaseId))
                        .placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                        .error(R.drawable.ic_coverartarchive_logo_no_text)
                        .size(100, 100)
                        .crossfade(true)
                        .build(),
                    contentDescription = it.releaseName,
                    placeholder = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(1f)
                        .clickable {
                            showBottomSheet = true
                            hueSoundViwModel.changeSelectedRelease(it)
                        }
                )
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = wheelOffset * size.height
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        val orientation = LocalConfiguration.current.orientation
        val wheelHeight = with(LocalDensity.current) {
            when (orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> (LocalConfiguration.current.screenHeightDp / 2).dp
                else -> (LocalConfiguration.current.screenWidthDp / 2).dp
            }
        }
        SmoothIndependentColorWheels(
            modifier = Modifier
                .height(wheelHeight)
        ) {
            hueSoundViwModel.onColorPickled(it.toHex())
        }
    }


}


@Composable
fun SmoothIndependentColorWheels(
    modifier: Modifier = Modifier,
    outerWheelColors: List<Color> = defaultColorList(),
    innerWheelWidth: Float = 0.5f,
    onColorSelected: (Color) -> Unit
) {
    val outerWheelRotation = remember { Animatable(0f) }
    val innerWheelRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    var interactingWheel by remember { mutableStateOf(WheelType.NONE) }
    var previousTouchAngle by remember { mutableStateOf(0f) }

    var selectedOuterColorIndex by rememberSaveable { mutableStateOf(0) }
    var selectedShadeIndex by rememberSaveable { mutableStateOf(5) }

    var lastDragTime by remember { mutableStateOf(0L) }
    var velocity by remember { mutableStateOf(0f) }


    val colorShades = remember(selectedOuterColorIndex) {
        generateShades(outerWheelColors[selectedOuterColorIndex], 12)
    }

    LaunchedEffect(colorShades, selectedShadeIndex) {
        onColorSelected(colorShades[selectedShadeIndex])
    }

    Box(
        modifier = modifier
            .aspectRatio(2f)
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

                            scope.launch {
                                when (interactingWheel) {
                                    WheelType.OUTER -> outerWheelRotation.stop()
                                    WheelType.INNER -> innerWheelRotation.stop()
                                    else -> {}
                                }
                            }
                        },
                        onDragEnd = {
                            val wheelToAnimate = interactingWheel
                            val targetVelocity = velocity * 1.2f
                            interactingWheel = WheelType.NONE
                            scope.launch {
                                when (wheelToAnimate) {
                                    WheelType.OUTER -> {
                                        outerWheelRotation.animateDecay(
                                            initialVelocity = targetVelocity,
                                            animationSpec = exponentialDecay(
                                                frictionMultiplier = 2.5f,
                                                absVelocityThreshold = 0.6f
                                            )
                                        )
                                    }

                                    WheelType.INNER -> {
                                        innerWheelRotation.animateDecay(
                                            initialVelocity = targetVelocity,
                                            animationSpec = exponentialDecay(
                                                frictionMultiplier = 2.5f,
                                                absVelocityThreshold = 0.6f
                                            )
                                        )
                                    }

                                    else -> {}
                                }
                            }
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

                            val currentTime = System.currentTimeMillis()
                            val timeDiff = (currentTime - lastDragTime).coerceAtLeast(1)
                            velocity = (angleDiff / timeDiff) * 1000f

                            // Apply rotation to the appropriate wheel
                            scope.launch {
                                when (interactingWheel) {
                                    WheelType.OUTER -> {
                                        outerWheelRotation.snapTo(outerWheelRotation.value + angleDiff)
                                    }

                                    WheelType.INNER -> {
                                        innerWheelRotation.snapTo(innerWheelRotation.value + angleDiff)
                                    }

                                    else -> {}
                                }
                            }


                            previousTouchAngle = currentAngle
                            lastDragTime = currentTime
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
                                (rawAngle - innerWheelRotation.value + 360) % 360
                            }

                            distanceFromCenter <= canvasRadius -> {
                                (rawAngle - outerWheelRotation.value + 360) % 360
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

            rotate(outerWheelRotation.value, center) {
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

            rotate(innerWheelRotation.value, center) {
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

fun Color.toHex(): String {
    val argb = this.toArgb()
    return String.format("%06X", (argb and 0xFFFFFF))
}

fun Offset.distanceTo(other: Offset): Float {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}

@Preview
@Composable
private fun CircularWheelPreview() {
    SmoothIndependentColorWheels {}
}