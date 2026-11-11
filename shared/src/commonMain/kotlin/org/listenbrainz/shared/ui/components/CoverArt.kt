package org.listenbrainz.shared.ui.components

import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.playlist_card_bg1
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import org.listenbrainz.shared.util.CoverArtImageLinkExtractor
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Composable to display cover art images in a grid layout.
 * @param modifier Modifier to be applied to the layout.
 * @param coverArt Cover art svg link.
 * @param maxGridSize Number of images to be displayed in each row and column.
 * @param errorImage Drawable resource to be displayed in case of error.
 * @param areImagesClickable Boolean to determine if images are clickable.
 */
@Composable
fun CoverArtComposable(
    modifier: Modifier = Modifier,
    coverArt: String?,
    maxGridSize: Int,
    errorImage: DrawableResource = Res.drawable.playlist_card_bg1,
    areImagesClickable: Boolean = false,
) {
    if (coverArt == null) {
        Box(modifier) {
            Image(
                painterResource(errorImage),
                contentDescription = "Error Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        val uriHandler = LocalUriHandler.current
        val (imageLinks, anchorLinks, actualGridSize) = remember(coverArt) {
            val noOfImages = CoverArtImageLinkExtractor.getImageCount(coverArt)
            val imageLinks = CoverArtImageLinkExtractor.extractImageLinks(coverArt).toMutableList()
            val anchorLinks = CoverArtImageLinkExtractor.extractAnchorLinks(coverArt).toMutableList()
            var gridSize = maxGridSize
            if(noOfImages < maxGridSize * maxGridSize) {
                gridSize = floor(sqrt(noOfImages.toDouble())).toInt()
            }
            Triple(imageLinks.toList(), anchorLinks.toList(), gridSize)
        }

        Box(modifier) {
            ImageGridCover(
                imageUrls = imageLinks, columns = actualGridSize, errorImage = errorImage,
                anchorLinks = anchorLinks,
                isClickable = areImagesClickable,
                onClickImage = {
                    uriHandler.openUri(it)
                }
            )
        }
    }
}


@Composable
private fun ImageGridCover(
    modifier: Modifier = Modifier,
    imageUrls: List<String>,
    columns: Int,
    errorImage: DrawableResource,
    anchorLinks: List<String>,
    isClickable: Boolean,
    onClickImage: (String) -> Unit
) {
    val rows = min(ceil(imageUrls.size / columns.toFloat()).toInt(), columns) // Calculate number of rows
    val getModifier:(Int) -> Modifier = {index->
        if(isClickable)
            Modifier.clickable {
                onClickImage(anchorLinks[index])
            }
        else
            Modifier
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (colIndex in 0 until columns) {
                    val imageIndex = rowIndex * columns + colIndex
                    if (imageIndex < imageUrls.size) {
                        Box(
                            modifier = getModifier(imageIndex)
                                .weight(1f) // Distribute space equally in the row
                                .aspectRatio(1f) // Keeps images square
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(imageUrls[imageIndex])
                                    .build(),
                                fallback = painterResource(errorImage),
                                error = painterResource(errorImage),
                                contentDescription = "Grid Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

