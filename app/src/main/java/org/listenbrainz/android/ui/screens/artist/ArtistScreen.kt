package org.listenbrainz.android.ui.screens.artist

import ArtistLinksEnum
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.album.ReleaseGroupData
import org.listenbrainz.android.model.artist.Artist
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.model.artist.ReleaseGroup
import org.listenbrainz.android.model.artist.Tag
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ButtonLB
import org.listenbrainz.android.ui.components.CoverArtComposable
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.screens.profile.stats.ArtistCard
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_light
import org.listenbrainz.android.ui.theme.app_bg_mid
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.util.Utils.measureSize
import org.listenbrainz.android.util.Utils.removeHtmlTags
import org.listenbrainz.android.util.Utils.showToast
import org.listenbrainz.android.viewmodel.ArtistViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import kotlin.math.max
import kotlin.math.round

@Composable
fun ArtistScreen(
    artistMbid: String,
    viewModel: ArtistViewModel = hiltViewModel(),
    socialViewModel: SocialViewModel = hiltViewModel(),
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String) -> Unit,
    goToAlbumPage: (String) -> Unit,
    snackBarState: SnackbarHostState,
    topBarActions: TopBarActions
) {
    LaunchedEffect(Unit) {
        viewModel.fetchArtistData(artistMbid)
    }
    val uiState by viewModel.uiState.collectAsState()
    ArtistScreen(
        artistMbid = artistMbid,
        uiState = uiState,
        goToArtistPage = goToArtistPage,
        goToUserPage = goToUserPage,
        socialViewModel = socialViewModel,
        snackBarState = snackBarState,
        goToAlbumPage = goToAlbumPage,
        topBarActions = topBarActions
    )
}

@Composable
private fun ArtistScreen(
    artistMbid: String,
    uiState: ArtistUIState,
    topBarActions: TopBarActions,
    goToArtistPage: (String) -> Unit,
    goToUserPage: (String) -> Unit,
    socialViewModel: SocialViewModel,
    snackBarState: SnackbarHostState,
    goToAlbumPage: (String) -> Unit,
) {
    Column {
        TopBar(
            modifier = Modifier.statusBarsPadding(),
            topBarActions = topBarActions,
            title = AppNavigationItem.Artist.title
        )
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = uiState.isLoading,
            contentAlignment = Alignment.Center
        ) { isLoading ->
            if (isLoading) {
                LoadingAnimation()
            } else {
                LazyColumn {
                    item {
                        BioCard(
                            header = uiState.name,
                            coverArt = uiState.coverArt,
                            displayRadioButton = true,
                            beginYear = uiState.beginYear,
                            area = uiState.area,
                            totalPlays = uiState.totalPlays,
                            totalListeners = uiState.totalListeners,
                            wikiExtract = uiState.wikiExtract,
                            artistTags = uiState.tags,
                            artistMbid = uiState.artistMbid
                        )
                    }
                    item {
                        Links(uiState.linksMap)
                    }
                    item {
                        PopularTracks(
                            uiState = uiState,
                            goToArtistPage = goToArtistPage,
                            snackbarState = snackBarState
                        )
                    }
                    item {
                        AlbumsCard(
                            header = "Albums",
                            albumsList = uiState.albums,
                            goToAlbumPage = goToAlbumPage
                        )
                    }
                    item {
                        AlbumsCard(
                            header = "Appears On",
                            albumsList = uiState.appearsOn,
                            goToAlbumPage = goToAlbumPage
                        )
                    }
                    item {
                        SimilarArtists(uiState = uiState, goToArtistPage = goToArtistPage)
                    }
                    item {
                        TopListenersCard(uiState = uiState, goToUserPage = goToUserPage)
                    }
                    item {
                        if (uiState.name != null) {
                            ReviewsCard(
                                reviewOfEntity = uiState.reviews,
                                goToUserPage = goToUserPage,
                                socialViewModel = socialViewModel,
                                artistMbid = artistMbid,
                                artistName = uiState.name,
                                snackBarState = snackBarState,
                                onMessageShown = { socialViewModel.clearMsgFlow() },
                                onErrorShown = { socialViewModel.clearErrorFlow() })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BioCard(
    header: String? = null,
    coverArt: String? = null,
    useWebView: Boolean = true,
    displayRadioButton: Boolean = false,
    beginYear: Int? = null,
    area: String? = null,
    totalPlays: Int? = 0,
    totalListeners: Int? = 0,
    wikiExtract: ArtistWikiExtract? = null,
    artistTags: Tag? = null,
    artists: List<Artist?>? = null,
    artistMbid: String? = null,
    albumType: String? = null,
    albumReleaseDate: String? = null,
    albumTags: List<ReleaseGroupData?>? = null
) {
    //Surface is added to make the rounded corner shape of Box visible
    Surface(
        modifier = Modifier,
        color = ListenBrainzTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ListenBrainzTheme.paddings.largePadding)
        ) {
            Column {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeLayout { constraints ->
                        val radioButtonPlaceables = subcompose("radioButton") {
                            if (displayRadioButton) {
                                val uriHandler = LocalUriHandler.current
                                val context = LocalContext.current
                                LbRadioButton(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .wrapContentWidth()
                                ) {
                                    if (artistMbid != null) {
                                        uriHandler.openUri("https://listenbrainz.org/explore/lb-radio/?prompt=artist:($artistMbid)&mode=easy")
                                    } else {
                                        context.showToast("Sorry, cannot play radio right now.")
                                    }
                                }
                            }
                        }.map { measurable: Measurable ->
                            measurable.measure(constraints)
                        }

                        val radioButtonSize = radioButtonPlaceables.measureSize()

                        val textPlaceables = subcompose("game") {
                            Text(
                                modifier = Modifier,
                                text = header ?: "",
                                color = ListenBrainzTheme.colorScheme.text,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp)
                            )
                        }.map { measurable: Measurable ->
                            measurable.measure(
                                constraints.copy(
                                    maxWidth = (constraints.maxWidth - radioButtonSize.width)
                                        .coerceAtLeast(constraints.minWidth),
                                )
                            )
                        }

                        val textSize = textPlaceables.measureSize()

                        val height = max(textSize.height, radioButtonSize.height)

                        layout(
                            constraints.maxWidth,
                            height
                        ) {
                            textPlaceables.forEach { placeable ->
                                placeable.placeRelative(0, (height - textSize.height) / 2)
                            }

                            radioButtonPlaceables.forEach { placeable ->
                                placeable.placeRelative(
                                    constraints.maxWidth - radioButtonSize.width,
                                    (height - radioButtonSize.height) / 2
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row {
                    if (coverArt != null) {
                        if (useWebView) {
                            CoverArtComposable(
                                coverArt = coverArt,
                                maxGridSize = 3,
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(coverArt)
                                    .build(),
                                fallback = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                                modifier = Modifier.size(ListenBrainzTheme.sizes.listenCardHeight * 3f),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                                filterQuality = FilterQuality.Low,
                                contentDescription = "Album Cover Art"
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                        }

                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        if (beginYear != null) {
                            Text(
                                beginYear.toString(),
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        } else if (artists != null) {
                            Row {
                                artists.map {
                                    Text(
                                        (it?.name ?: "") + (it?.joinPhrase ?: ""),
                                        color = app_bg_mid,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (area != null) {
                            Text(
                                area.toString(),
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                        if (albumType != null) {
                            Text(
                                albumType.toString(),
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                        if (albumReleaseDate != null) {
                            Text(
                                albumReleaseDate.toString(),
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            color = ListenBrainzTheme.colorScheme.dividerColor,
                            thickness = 3.dp,
                            modifier = Modifier.padding(end = 50.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.listens_icon),
                                contentDescription = null,
                                tint = app_bg_mid
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                formatNumber(totalPlays ?: 0) + " plays",
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                        Row {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.listeners_icon),
                                contentDescription = null,
                                tint = app_bg_mid
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                formatNumber(totalListeners ?: 0) + " listeners",
                                color = app_bg_mid,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }
                if (wikiExtract?.wikipediaExtract?.content != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        removeHtmlTags(wikiExtract.wikipediaExtract.content).trim(),
                        maxLines = 4,
                        color = app_bg_mid,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        overflow = TextOverflow.Ellipsis
                    )
                    if (wikiExtract.wikipediaExtract.url != null) {
                        val uriHandlder = LocalUriHandler.current
                        Text(
                            "read more",
                            color = ListenBrainzTheme.colorScheme.followerChipSelected,
                            modifier = Modifier.clickable {
                                uriHandlder.openUri(wikiExtract.wikipediaExtract.url)
                            })
                    }
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 10.dp)
                ) {
                    if (artistTags != null) {
                        artistTags.artist?.map {
                            if (it.tag != null) {
                                BioTag(it.tag, it.count ?: 0)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                    albumTags?.map {
                        if (it?.tag != null) {
                            BioTag(it.tag, it.count ?: 0)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BioTag(tag: String, count: Int) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape((14.dp))
            )
            .background(ListenBrainzTheme.colorScheme.followerCardColor)
            .padding(horizontal = ListenBrainzTheme.paddings.smallPadding, vertical = 6.dp)
    ) {
        Row {
            Text(
                tag,
                color = ListenBrainzTheme.colorScheme.followerChipSelected,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                count.toString(),
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }
    }
}

class LinkCardData(val iconResId: Int, val label: String, val url: String) {}

@Composable
fun Links(
    linksMap: Map<ArtistLinksEnum, List<LinkCardData>>
) {
    val linkOptionSelectionState: MutableState<ArtistLinksEnum> = remember {
        mutableStateOf(ArtistLinksEnum.MAIN)
    }
    Box(
        modifier = Modifier
            .background(brush = ListenBrainzTheme.colorScheme.userPageGradient)
            .fillMaxWidth()
            .padding(ListenBrainzTheme.paddings.largePadding)
    ) {
        Column {
            Text(
                "Links",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = ListenBrainzTheme.paddings.vertical)
            ) {
                repeat(5) { position ->
                    val reqdState = when (position) {
                        0 -> linkOptionSelectionState.value == ArtistLinksEnum.ALL
                        1 -> linkOptionSelectionState.value == ArtistLinksEnum.MAIN
                        2 -> linkOptionSelectionState.value == ArtistLinksEnum.STREAMING
                        3 -> linkOptionSelectionState.value == ArtistLinksEnum.SOCIAL_MEDIA
                        4 -> linkOptionSelectionState.value == ArtistLinksEnum.LYRICS
                        else -> false
                    }
                    ElevatedSuggestionChip(
                        onClick = {
                            when (position) {
                                0 -> linkOptionSelectionState.value = ArtistLinksEnum.ALL
                                1 -> linkOptionSelectionState.value = ArtistLinksEnum.MAIN
                                2 -> linkOptionSelectionState.value = ArtistLinksEnum.STREAMING
                                3 -> linkOptionSelectionState.value = ArtistLinksEnum.SOCIAL_MEDIA
                                4 -> linkOptionSelectionState.value = ArtistLinksEnum.LYRICS
                            }
                        },
                        label = {
                            val label = when (position) {
                                0 -> ArtistLinksEnum.ALL.label
                                1 -> ArtistLinksEnum.MAIN.label
                                2 -> ArtistLinksEnum.STREAMING.label
                                3 -> ArtistLinksEnum.SOCIAL_MEDIA.label
                                4 -> ArtistLinksEnum.LYRICS.label
                                else -> ""
                            }
                            Text(
                                label, color = when (reqdState) {
                                    true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                                    false -> ListenBrainzTheme.colorScheme.followerChipSelected
                                }, style = ListenBrainzTheme.textStyles.chips
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = when (reqdState) {
                            true -> null
                            false -> BorderStroke(1.dp, lb_purple_night)
                        },
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            if (reqdState) {
                                ListenBrainzTheme.colorScheme.followerChipSelected
                            } else {
                                ListenBrainzTheme.colorScheme.followerChipUnselected
                            }
                        ),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

            }
            Column(modifier = Modifier.padding(top = 20.dp)) {
                val items = linksMap[linkOptionSelectionState.value]
                items?.chunked(3)?.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            LinkCard(
                                icon = ImageVector.vectorResource(item.iconResId),
                                label = item.label,
                                url = item.url,
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun PopularTracks(
    uiState: ArtistUIState,
    goToArtistPage: (String) -> Unit,
    snackbarState: SnackbarHostState,
) {
    var popularTracksCollapsibleState by rememberSaveable { mutableStateOf(true) }

    val popularTracks = when (popularTracksCollapsibleState) {
        true -> uiState.popularTracks?.take(5) ?: listOf()
        false -> uiState.popularTracks ?: listOf()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(start = 23.dp, end = 23.dp, top = 23.dp)
    ) {
        Column {
            Text(
                "Popular Tracks",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            popularTracks.forEach { track ->
                val metadata = Metadata(
                    trackMetadata = TrackMetadata(
                        additionalInfo = null,
                        artistName = track?.artistName ?: "",
                        mbidMapping = MbidMapping(
                            artists = track?.artists,
                            recordingMbid = track?.recordingMbid,
                            recordingName = track?.recordingName ?: "",
                            caaReleaseMbid = track?.caaReleaseMbid,
                            caaId = track?.caaId,
                            artistMbids = track?.artistMbids ?: emptyList()
                        ),
                        releaseName = null,
                        trackName = track?.recordingName ?: ""
                    ),
                )

                ListenCardSmallDefault(
                    metadata = metadata,
                    coverArtUrl = Utils.getCoverArtUrl(track?.caaReleaseMbid, track?.caaId),
                    goToArtistPage = goToArtistPage,
                    onDropdownError = { error ->
                        snackbarState.showSnackbar(error.toast)
                    },
                    onDropdownSuccess = { message ->
                        snackbarState.showSnackbar(message)
                    }
                ) {
                    // No playback action for now
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if ((uiState.popularTracks?.size ?: 0) > 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = popularTracksCollapsibleState,
                        onClick = {
                            popularTracksCollapsibleState = !popularTracksCollapsibleState
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumsCard(
    header: String,
    albumsList: List<ReleaseGroup?>?,
    goToAlbumPage: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(23.dp)
    ) {
        Column {
            Text(
                header,
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
            )
            LazyRow(
                modifier = Modifier
                    .padding(top = 20.dp)
            ) {
                items(albumsList?.size ?: 0) {
                    val album = albumsList?.get(it)
                    Box(modifier = Modifier
                        .width(150.dp)
                        .clickable {
                            if (album?.mbid != null) {
                                goToAlbumPage(album.mbid)
                            }
                        }) {
                        Column {
                            val coverArt =
                                Utils.getCoverArtUrl(album?.caaReleaseMbid, album?.caaId, 500)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(coverArt)
                                    .build(),
                                fallback = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .size(ListenBrainzTheme.sizes.listenCardHeight * 3f),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_coverartarchive_logo_no_text),
                                filterQuality = FilterQuality.Low,
                                contentDescription = "Album Cover Art"
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(album?.name ?: "",
                                color = ListenBrainzTheme.colorScheme.followerCardTextColor,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    if (album?.mbid != null) {
                                        goToAlbumPage(album.mbid)
                                    }
                                })
                        }
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

        }
    }
}

@Composable
private fun SimilarArtists(
    uiState: ArtistUIState,
    goToArtistPage: (String) -> Unit
) {
    val similarArtistsCollapisbleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val similarArtists = when (similarArtistsCollapisbleState.value) {
        true -> uiState.similarArtists?.take(5) ?: listOf()
        false -> uiState.similarArtists ?: listOf()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(23.dp)
    ) {
        Column {
            Text(
                "Similar Artists",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            similarArtists.map {
                ArtistCard(artistName = it?.name ?: "") {
                    if (it?.artistMbid != null)
                        goToArtistPage(it.artistMbid)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if ((uiState.similarArtists?.size ?: 0) > 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = similarArtistsCollapisbleState.value
                    ) {
                        similarArtistsCollapisbleState.value = !similarArtistsCollapisbleState.value
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TopListenersCard(
    uiState: ArtistUIState,
    goToUserPage: (String) -> Unit,
) {
    val topListenersCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    val topListeners = when (topListenersCollapsibleState.value) {
        true -> uiState.topListeners?.take(5) ?: listOf()
        false -> uiState.topListeners ?: listOf()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(23.dp)
    ) {
        Column {
            Text(
                "Top Listeners",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            topListeners.map {
                ArtistCard(
                    artistName = it?.userName ?: "",
                    listenCountLabel = formatNumber(it?.listenCount ?: 0)
                ) {
                    if (it?.userName != null) {
                        goToUserPage(it.userName)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if ((uiState.topListeners?.size ?: 0) > 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = topListenersCollapsibleState.value
                    ) {
                        topListenersCollapsibleState.value = !topListenersCollapsibleState.value
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ReviewsCard(
    reviewOfEntity: CBReview?,
    socialViewModel: SocialViewModel,
    snackBarState: SnackbarHostState,
    goToUserPage: (String) -> Unit,
    artistMbid: String? = null,
    artistName: String? = null,
    onErrorShown: () -> Unit,
    onMessageShown: () -> Unit,
    albumMbid: String? = null,
    albumName: String? = null,
) {
    val reviews = reviewOfEntity?.reviews.orEmpty().take(2)
    val socialUiState by socialViewModel.uiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            .padding(23.dp)
    ) {
        Column {
            Text(
                "Reviews",
                color = ListenBrainzTheme.colorScheme.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
            )
            if (reviews.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Be the first one to review this artist on CritiqueBrainz",
                    color = app_bg_mid,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                reviews.map {
                    Box {
                        Column {
                            Row {
                                Text("Rating: ", color = app_bg_light)
                                RatingBar(
                                    modifier = Modifier.padding(start = 2.dp),
                                    value = (it?.rating ?: 0).toFloat(),
                                    size = 19.dp,
                                    spaceBetween = 2.dp,
                                    style = RatingBarStyle.Default,
                                    onValueChange = {},
                                    onRatingChanged = {}
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                it?.text ?: "",
                                color = app_bg_mid,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                "By ${it?.user?.musicbrainzUsername ?: ""}",
                                color = lb_purple_night,
                                modifier = Modifier.clickable {
                                    if (it?.user?.musicbrainzUsername != null) {
                                        goToUserPage(it.user.musicbrainzUsername)
                                    }
                                })
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            var showReviewDialog by rememberSaveable {
                mutableStateOf(false)
            }

            ButtonLB(
                onClick = { showReviewDialog = !showReviewDialog }
            ) {
                Text("Review")
            }

            if (showReviewDialog) {
                ReviewDialog(
                    trackName = null,
                    releaseName = albumName,
                    artistName = artistName,
                    reviewEntityType = when {
                        albumMbid != null-> ReviewEntityType.RELEASE_GROUP
                        artistMbid != null -> ReviewEntityType.ARTIST
                        else -> return
                    },
                    onDismiss = { showReviewDialog = false },
                    isCritiqueBrainzLinked = socialViewModel::isCritiqueBrainzLinked,
                    onSubmit = { type, blurbContent, rating, locale ->
                        val metadata = Metadata(
                            trackMetadata = TrackMetadata(
                                artistName = artistName.orEmpty(),
                                releaseName = albumName,
                                trackName = "",
                                additionalInfo = null,
                                mbidMapping = MbidMapping(
                                    releaseMbid = albumMbid,
                                    recordingName = "",
                                    artistMbids = listOf(artistMbid.orEmpty())
                                )
                            )
                        )
                        socialViewModel.review(
                            metadata,
                            type,
                            blurbContent,
                            rating,
                            locale
                        )
                    }
                )
            }
        }
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown)
    SuccessBar(
        resId = socialUiState.successMsgId,
        onMessageShown = onMessageShown,
        snackbarState = snackBarState
    )
}

@Composable
private fun LinkCard(
    icon: ImageVector,
    label: String,
    url: String,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = ListenBrainzTheme.colorScheme.followerCardColor)
            .padding(top = 10.dp, bottom = 10.dp, start = 16.dp, end = 16.dp)
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (err: Error) {
                    Toast
                        .makeText(context, "Some unknown error occurred", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, contentDescription = null, tint = when (icon) {
                    ImageVector.vectorResource(id = R.drawable.musicbrainz_logo) -> Color.Unspecified
                    else -> ListenBrainzTheme.colorScheme.followerCardTextColor
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                label,
                color = ListenBrainzTheme.colorScheme.followerCardTextColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
private fun LbRadioButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonColors(
            containerColor = Color(0xFF353070),
            contentColor = Color.White,
            disabledContentColor = Color(0xFF353070),
            disabledContainerColor = Color(0xFF353070)
        )
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.lb_radio_play_button),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Radio",
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * A Composable function that renders an SVG string using a WebView.
 *
 * Previously used to display cover art in Artist and Album screens.
 * Although currently unused, this function can be reused for rendering inline SVG content
 * with flexible dimensions in the future.
 *
 * @param svgContent The raw SVG markup as a string.
 * @param width The desired width of the WebView.
 * @param height The desired height of the WebView.
 */
@Composable
fun SvgWithWebView(svgContent: String, width: Dp, height: Dp) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    LaunchedEffect(svgContent) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Optionally, handle size adjustments or interactions if needed
            }
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        overflow: auto; /* Ensure scrollbars appear if necessary */
                    }
                    svg {
                        width: 100%;
                        height: auto; /* Adjust to fit content */
                    }
                </style>
            </head>
            <body>
                <div id="svg-container">
                    $svgContent
                </div>
            </body>
            </html>
        """

        webView.loadDataWithBaseURL(
            null,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    AndroidView(
        factory = { webView },
        update = { view -> },
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
    )
}



fun formatNumber(input: Int): String {
    return when {
        input >= 1_00_000 -> "${round(input / 1_00_000f)}L"
        input >= 1_000 -> "${round(input / 1_000f)}K"
        else -> input.toString()
    }
}