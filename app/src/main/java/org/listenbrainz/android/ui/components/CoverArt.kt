package org.listenbrainz.android.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.util.CoverArtImageLinkExtractor
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

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
    errorImage: Int = R.drawable.playlist_card_bg1,
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
        val context = LocalContext.current
        val (imageLinks, anchorLinks, actualGridSize) = remember(coverArt) {
            val noOfImages = CoverArtImageLinkExtractor.getImageCount(coverArt)
            val imageLinks = CoverArtImageLinkExtractor.extractImageLinks(coverArt).toMutableList()
            val anchorLinks = CoverArtImageLinkExtractor.extractAnchorLinks(coverArt).toMutableList()
            var gridSize = maxGridSize
            if(noOfImages < maxGridSize * maxGridSize) {
                gridSize = floor(Math.sqrt(noOfImages.toDouble())).toInt()
            }
            Triple(imageLinks.toList(), anchorLinks.toList(), gridSize)
        }

        Box(modifier) {
            ImageGridCover(
                imageUrls = imageLinks, columns = actualGridSize, errorImage = errorImage,
                anchorLinks = anchorLinks,
                isClickable = areImagesClickable,
                onClickImage = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    context.startActivity(intent)
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
    errorImage: Int,
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
                                model = ImageRequest.Builder(LocalContext.current)
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

