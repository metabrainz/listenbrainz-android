package org.listenbrainz.android.ui.screens.artist

import ArtistLinksEnum
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.app_bg_dark
import org.listenbrainz.android.ui.theme.app_bg_mid
import org.listenbrainz.android.ui.theme.app_bg_secondary_dark
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Constants.MB_BASE_URL
import org.listenbrainz.android.viewmodel.ArtistViewModel


@Composable
fun ArtistScreen(
    artistMbid: String,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchArtistData(artistMbid)
    }
    val uiState by viewModel.uiState.collectAsState()
    ArtistScreen(artistMbid = artistMbid,uiState = uiState)
}

@Composable
private fun ArtistScreen(
    artistMbid: String,
    uiState: ArtistUIState
) {
    Box(modifier = Modifier.fillMaxSize()){
        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
        AnimatedVisibility(visible = !uiState.isLoading) {
            ListenBrainzTheme {
                LazyColumn {
                    item {
                        ArtistBioCard(uiState = uiState)
                    }
                    item { 
                        Links(uiState = uiState, artistMbid = artistMbid)
                    }
                    item {

                    }
                }
            }

        }
    }
}

@Composable
private fun ArtistBioCard(
    uiState: ArtistUIState
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
        .background(Color(0xFF2B2E35))
        .padding(23.dp)){
        Column {
            Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(uiState.name ?: "", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 25.sp))
                LbRadioButton {

                }
            }
            Row {
                if (uiState.coverArt != null) {
                    SvgWithWebView(
                        svgContent = uiState.coverArt,
                        width = 200.dp,
                        height = 200.dp
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(uiState.beginYear.toString(), color = app_bg_mid, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                    Text(uiState.area.toString(), color = app_bg_mid, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = app_bg_dark, thickness = 3.dp, modifier = Modifier.padding(end = 50.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.listens_icon),
                            contentDescription = null,
                            tint = app_bg_mid
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text((uiState.totalPlays ?: 0).toString() + " plays", color = app_bg_mid, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                    }
                    Row {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.listeners_icon),
                            contentDescription = null,
                            tint = app_bg_mid
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text((uiState.totalListeners ?: 0).toString() + " listeners", color = app_bg_mid, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                    }
                }
            }
            if(uiState.wikiExtract?.wikipediaExtract?.content != null){
                Spacer(modifier = Modifier.height(20.dp))
                Text(removeHtmlTags(uiState.wikiExtract.wikipediaExtract.content).trim() , maxLines = 4, color = app_bg_mid, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp), overflow = TextOverflow.Ellipsis)
                if(uiState.wikiExtract.wikipediaExtract.url != null){
                    val uriHandlder = LocalUriHandler.current
                    Text("read more", color = lb_purple_night, modifier = Modifier.clickable {
                        uriHandlder.openUri(uiState.wikiExtract.wikipediaExtract.url)
                    })
                }
            }
            Row (modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 10.dp)) {
                uiState.tags?.artist?.map {
                    if(it.tag != null){
                        Box (modifier = Modifier
                            .clip(
                                RoundedCornerShape((16.dp))
                            )
                            .background(app_bg_secondary_dark)
                            .padding(10.dp)) {
                            Row {
                                Text(it.tag, color= lb_purple_night, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text((it.count ?: 0).toString(), color = Color.White ,style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                }
            }

        }
    }
}

class LinkCardData (val iconResId: ImageVector, val label: String, val url: String) {}

@Composable
private fun Links(
    artistMbid: String,
    uiState: ArtistUIState
) {
    //TODO: Move this logic to vm and get map to ui state
    val allLinkCards: MutableList<LinkCardData> = mutableListOf()
    val mainLinkCards: MutableList<LinkCardData> = mutableListOf()
    val streamingLinkCards: MutableList<LinkCardData> = mutableListOf()
    val socialMediaLinkCards: MutableList<LinkCardData> = mutableListOf()
    val lyricsLinkCards: MutableList<LinkCardData> = mutableListOf()
    val links = uiState.links
    if(links?.wikidata != null){
        val wikidata = LinkCardData(ImageVector.vectorResource(id = R.drawable.wiki_data), "Wikidata", links.wikidata)
        allLinkCards.add(wikidata)
        mainLinkCards.add(wikidata)
    }
    if(links?.lyrics != null){
        val lyrics = LinkCardData(Icons.Default.SettingsVoice, "Lyrics", links.lyrics)
        allLinkCards.add(lyrics)
        lyricsLinkCards.add(lyrics)
    }
    if(links?.officialHomePage != null){
        val homePage = LinkCardData(ImageVector.vectorResource(id = R.drawable.home_icon), "Homepage", links.officialHomePage)
        allLinkCards.add(homePage)
        mainLinkCards.add(homePage)
    }
    if(links?.purchaseForDownload != null){
        val purchase = LinkCardData(ImageVector.vectorResource(id = R.drawable.mail_order), "Purchase for Download", links.purchaseForDownload)
        allLinkCards.add(purchase)
        streamingLinkCards.add(purchase)
    }
    if(links?.purchaseForMailOrder != null){
        val mailOrder = LinkCardData(ImageVector.vectorResource(id = R.drawable.mail_order), "Purchase for mail order", links.purchaseForMailOrder)
        allLinkCards.add(mailOrder)
        streamingLinkCards.add(mailOrder)
    }
    mainLinkCards.add(LinkCardData(ImageVector.vectorResource(id = R.drawable.musicbrainz_logo), "Edit", MB_BASE_URL + "artist/${artistMbid}"))
    val linksMap: Map<ArtistLinksEnum, List<LinkCardData>> = mapOf(
        ArtistLinksEnum.ALL to allLinkCards,
        ArtistLinksEnum.MAIN to mainLinkCards,
        ArtistLinksEnum.LYRICS to lyricsLinkCards,
        ArtistLinksEnum.STREAMING to streamingLinkCards,
        ArtistLinksEnum.SOCIAL_MEDIA to socialMediaLinkCards
    )
    val linkOptionSelectionState: MutableState<ArtistLinksEnum> = remember {
        mutableStateOf(ArtistLinksEnum.MAIN)
    }
    Box(modifier = Modifier
        //.background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
        .fillMaxWidth()
        .padding(23.dp)){
        Column {
            Text("Links", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp))
            Row (modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp)) {
                repeat(5) {
                    position ->
                    val reqdState = when(position){
                        0 -> linkOptionSelectionState.value == ArtistLinksEnum.ALL
                        1 -> linkOptionSelectionState.value == ArtistLinksEnum.MAIN
                        2 -> linkOptionSelectionState.value == ArtistLinksEnum.STREAMING
                        3 -> linkOptionSelectionState.value == ArtistLinksEnum.SOCIAL_MEDIA
                        4 -> linkOptionSelectionState.value == ArtistLinksEnum.LYRICS
                        else -> false
                    }
                    ElevatedSuggestionChip(
                        onClick = {
                            when(position){
                                0 -> linkOptionSelectionState.value = ArtistLinksEnum.ALL
                                1 -> linkOptionSelectionState.value = ArtistLinksEnum.MAIN
                                2 -> linkOptionSelectionState.value = ArtistLinksEnum.STREAMING
                                3 -> linkOptionSelectionState.value = ArtistLinksEnum.SOCIAL_MEDIA
                                4 -> linkOptionSelectionState.value = ArtistLinksEnum.LYRICS
                            }
                        },
                        label = {
                            val label = when(position){
                                0 -> ArtistLinksEnum.ALL.label
                                1 -> ArtistLinksEnum.MAIN.label
                                2 -> ArtistLinksEnum.STREAMING.label
                                3 -> ArtistLinksEnum.SOCIAL_MEDIA.label
                                4 -> ArtistLinksEnum.LYRICS.label
                                else -> ""
                            }
                            Text(label, color = when(reqdState){
                                true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                                false -> ListenBrainzTheme.colorScheme.followerChipSelected
                            }, style = ListenBrainzTheme.textStyles.chips)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = when(reqdState){
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
            Column (modifier = Modifier.padding(top = 20.dp)) {
                val items = linksMap[linkOptionSelectionState.value]
                items?.chunked(3)?.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            LinkCard(
                                icon = item.iconResId,
                                label = item.label,
                                url = item.url,
                            )
                        }
                        // Fill remaining empty space with spacers
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PopularTracks(
    uiState: ArtistUIState
) {

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
            .background(color = app_bg_secondary_dark)
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
        Row (verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = when(icon){
                ImageVector.vectorResource(id = R.drawable.musicbrainz_logo) -> Color.Unspecified
                    else -> lb_purple_night
            })
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = lb_purple_night, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
private fun LbRadioButton(
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, colors = ButtonColors(containerColor = Color(0xFF353070), contentColor = Color.White, disabledContentColor = Color(0xFF353070), disabledContainerColor = Color(0xFF353070))) {
        Row (horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.lb_radio_play_button),
                contentDescription = ""
            )
            Text("Radio")
        }
    }
}

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

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier
            .width(width)
            .height(height)
    )
}

fun removeHtmlTags(input: String): String {
    // Regular expression pattern to match HTML tags
    val regex = "<[^>]*>".toRegex()
    // Replace all matches of the pattern with an empty string
    return input.replace(regex, "")
}