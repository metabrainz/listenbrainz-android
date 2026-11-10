package org.listenbrainz.android.ui.screens.listeningNow

import android.content.res.Configuration
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.viewmodel.ListeningNowViewModel
import kotlin.math.max

@ExperimentalMaterialApi
private fun BackdropScaffoldState.offsetOrZero(): Float =
    try {
        requireOffset()
    } catch (_: IllegalStateException) {
        0f
    }

@ExperimentalMaterialApi
@Composable
fun ListeningNowBackDropScreen(
    modifier: Modifier = Modifier,
    backdropScaffoldState: BackdropScaffoldState,
    listeningNowViewModel: ListeningNowViewModel = koinViewModel(),
    paddingValues: PaddingValues,
    isLandscape: Boolean = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE,
    backLayerContent: @Composable () -> Unit
) {
    var maxDelta by rememberSaveable {
        mutableFloatStateOf(0F)
    }
    val defaultBackgroundColor = ListenBrainzTheme.colorScheme.background
    val listeningNowUIState by listeningNowViewModel.listeningNowUIState.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(listeningNowUIState.song) {
        if(!listeningNowUIState.isListeningNow && backdropScaffoldState.isConcealed){
            backdropScaffoldState.reveal()
        }
    }

    /** 56.dp is default bottom navigation height */
    val headerHeight by animateDpAsState(
        targetValue = if (isLandscape) 0.dp else
            if (!listeningNowUIState.isListeningNow)
                56.dp
            else
                56.dp + ListenBrainzTheme.sizes.frontLayerPeekHeight
    )
    BackdropScaffold(
        modifier = modifier.padding(top = paddingValues.calculateTopPadding()),
        frontLayerShape = RectangleShape,
        backLayerBackgroundColor = Color.Transparent,
        frontLayerScrimColor = Color.Unspecified,
        headerHeight = headerHeight, // 126.dp is optimal header height.
        peekHeight = 0.dp,
        scaffoldState = backdropScaffoldState,
        backLayerContent = backLayerContent,
        frontLayerBackgroundColor = defaultBackgroundColor,
        appBar = {},
        persistentAppBar = false,
        frontLayerContent = {
            LaunchedEffect(Unit) {
                val delta = backdropScaffoldState.offsetOrZero()
                maxDelta = max(delta, maxDelta)
            }

            LaunchedEffect(listeningNowUIState.song) {
                if (listeningNowUIState.isListeningNow) {
                    listeningNowViewModel.updatePalette()
                }
            }
            ListeningNowScreen(
                viewModel = listeningNowViewModel,
                backdropScaffoldState = backdropScaffoldState,
                gradientBox = {
                    val backgroundColor =
                        listeningNowUIState.palette?.lightBackgroundColor
                            ?: ListenBrainzTheme.colorScheme.background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val value =
                                    (backdropScaffoldState.offsetOrZero() / (maxDelta - headerHeight.toPx()))
                                alpha =
                                    if (value < 0.8f)
                                        (1f - value) * 0.25f else 0f
                            }
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        backgroundColor,
                                        Color(
                                            lerp(
                                                backgroundColor.value.toLong(),
                                                ListenBrainzTheme.colorScheme.background.value.toLong(),
                                                0.5f
                                            )
                                        )
                                    )
                                )
                            )
                    )
                }
            )
            if (!isLandscape && backdropScaffoldState.isRevealed) {
                ListeningNowCard(
                    uiState = listeningNowUIState,
                    isLandscape = false,
                    coroutineScope = scope,
                    backdropScaffoldState = backdropScaffoldState,
                    modifier = Modifier
                        .height(ListenBrainzTheme.sizes.frontLayerPeekHeight)
                        .graphicsLayer {
                            alpha =
                                (backdropScaffoldState.offsetOrZero() / (maxDelta - headerHeight.toPx()))
                        }
                )
            }

        })
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun ListeningNowBackDropScreenPreview() {
    ListeningNowBackDropScreen(
        backdropScaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed),
        paddingValues = PaddingValues(0.dp)
    ) {}
}
