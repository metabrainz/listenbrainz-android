package org.listenbrainz.android.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark

/**Small configuration of listen card.
 * This composable has fixed height used from [ListenBrainzTheme.sizes].
 * @param trailingContent **MUST** use the given modifier in its top level layout. */
@Composable
fun ListenCardSmall(
    modifier: Modifier = Modifier,
    releaseName: String,
    artistName: String,
    coverArtUrl: String?,
    shape: Shape = ListenBrainzTheme.shapes.listenCardSmall,
    @DrawableRes errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    enableDropdownIcon: Boolean = false,
    onDropdownIconClick: () -> Unit = {},
    enableTrailingContent: Boolean = false,
    /****MUST** use the given modifier in its top level layout.*/
    trailingContent: @Composable (modifier: Modifier) -> Unit = {},
    enableBlurbContent: Boolean = false,
    blurbContent: @Composable (modifier: Modifier) -> Unit = {},
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = true) { onClick() },
        shape = shape,
        shadowElevation = 4.dp,
        color = ListenBrainzTheme.colorScheme.level1
    ) {
        Column {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ListenBrainzTheme.sizes.listenCardHeight),
                contentAlignment = Alignment.CenterStart
            ) {
        
                val (mainContentFraction, trailingContentFraction, dropDownButtonFraction) = remember(enableDropdownIcon, enableTrailingContent) {
                    when {
                        enableDropdownIcon && enableTrailingContent -> Triple(0.60f, 0.80f, 0.20f)    // 0.20f for dropdown and 0.80f for trailing content
                        enableDropdownIcon && !enableTrailingContent -> Triple(0.90f, 0f, 1f) // 0.10f for dropdown
                        !enableDropdownIcon && enableTrailingContent -> Triple(0.70f, 1f, 0f)   // 0.30f for trailing content
                        else -> Triple(1f, 0f, 0f)
                    }
                }
        
                Row(
                    modifier = Modifier.fillMaxWidth(mainContentFraction),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
            
                    AlbumArt(coverArtUrl, errorAlbumArt)
            
                    Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))
            
                    TitleAndSubtitle(title = releaseName, subtitle = artistName)
            
                }
                
                Box(
                    modifier = modifier
                        .fillMaxWidth(1f - mainContentFraction)
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
            
                    // Trailing content
                    if (enableTrailingContent) {
                        trailingContent(
                            modifier = Modifier
                                .fillMaxWidth(trailingContentFraction)
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 6.dp)
                        )
                    }
            
                    // Dropdown Icon
                    if (enableDropdownIcon) {
                        DropdownButton(
                            modifier = Modifier
                                .fillMaxWidth(dropDownButtonFraction)
                                .align(Alignment.CenterEnd),
                            onDropdownIconClick = onDropdownIconClick
                        )
                    }
            
                }
        
            }
            
            if (enableBlurbContent) {
                Divider()
                blurbContent(modifier = Modifier
                    .padding(ListenBrainzTheme.paddings.insideCard)
                )
            }
        }
    }
}

@Composable
private fun DropdownButton(modifier: Modifier = Modifier, onDropdownIconClick: () -> Unit) {
    
    IconButton(
        modifier = modifier,
        onClick = onDropdownIconClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_options),
            contentDescription = "",
            tint = ListenBrainzTheme.colorScheme.hint,
            modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.insideCard)
        )
    }
}

@Composable
private fun AlbumArt(
    coverArtUrl: String?,
    errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text
) {
    // Use this for previews
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverArtUrl)
            .build(),
        fallback = painterResource(id = errorAlbumArt),
        modifier = Modifier.size(ListenBrainzTheme.sizes.listenCardHeight),
        contentScale = ContentScale.Fit,
        placeholder = painterResource(id = errorAlbumArt),
        filterQuality = FilterQuality.Low,
        contentDescription = "Album Cover Art"
    )
}

/** [title] corresponds to release name and [subtitle] corresponds to artist name.*/
@Composable
fun TitleAndSubtitle(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    alignment: Alignment.Horizontal = Alignment.Start,
    titleColor: Color = if (onScreenUiModeIsDark()) Color.White else lb_purple,
    subtitleColor: Color = (if (onScreenUiModeIsDark()) Color.White else lb_purple).copy(alpha = 0.7f)
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            text = title,
            style = ListenBrainzTheme.textStyles.listenTitle,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle.isNotEmpty()){
            Text(
                text = subtitle,
                style = ListenBrainzTheme.textStyles.listenSubtitle,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun ListenCardSmallPreview() {
    ListenBrainzTheme {
        ListenCardSmall(
            releaseName = "Title",
            artistName = "Artist",
            coverArtUrl = "",
            enableTrailingContent = true,
            enableBlurbContent = true,
            enableDropdownIcon = true,
            trailingContent = { modifier ->
                Column(modifier = modifier) {
                    TitleAndSubtitle(title = "Userrrrrrrrrrrrrr", subtitle = "60%")
                }
            },
            blurbContent = {
                Column(modifier = it) {
                    Text(text = "Hello", color = ListenBrainzTheme.colorScheme.text)
                }
                
            },
        ) {}
    }
}