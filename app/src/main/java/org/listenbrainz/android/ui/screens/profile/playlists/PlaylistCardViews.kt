package org.listenbrainz.android.ui.screens.profile.playlists

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.ui.components.CoverArtComposable
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

/**
 * Composable for Playlist Grid View Card
 * @param modifier Modifier
 * @param coverArt String? - Cover Art URL
 * @param title String - Title of the playlist
 * @param updatedDate String - The date when playlist was last updated
 * @param errorCoverArt Int - Image to display when cover art could not be loaded
 * @param onClickCard () -> Unit - Function to be called when card is clicked
 * @param onDropdownClick (PlaylistDropdownItems) -> Unit - Function to be called when dropdown item is clicked
 * @param canUserEdit Tells whether the current profile belongs to the logged in user
 */
@Composable
fun PlaylistGridViewCard(
    modifier: Modifier,
    coverArt: String?,
    title: String,
    updatedDate: String,
    @DrawableRes errorCoverArt: Int = R.drawable.playlist_card_bg1,
    onClickCard: () -> Unit,
    onDropdownClick: (PlaylistDropdownItems) -> Unit,
    canUserEdit: Boolean
) {
    var isDropdownEnabled by remember {
        mutableStateOf(false)
    }
    Card(
        onClick = onClickCard,
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        modifier = modifier
            .padding(8.dp)
            .width(150.dp)
            .clip(ListenBrainzTheme.shapes.listenCardSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // Image (Coil for async loading)
                CoverArtComposable(
                    modifier = Modifier.aspectRatio(1f),
                    coverArt = coverArt,
                    maxGridSize = PlaylistDataRepository.DEFAULT_PLAYLIST_GRID_SIZE,
                    errorImage = errorCoverArt,
                    areImagesClickable = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        color = ListenBrainzTheme.colorScheme.listenText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = ListenBrainzTheme.textStyles.dialogTitleBold.copy(fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row() {
                        Text(
                            text = "Updated $updatedDate",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = ListenBrainzTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(modifier = Modifier.width(40.dp), onClick = {
                            isDropdownEnabled = !isDropdownEnabled
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_options),
                                contentDescription = "Options button"
                            )
                        }

                    }
                    PlaylistDropdownMenu(
                        expanded = isDropdownEnabled,
                        onDismiss = { isDropdownEnabled = false },
                        onItemClick = {
                            onDropdownClick(it)
                            isDropdownEnabled = false
                        },
                        isPrivateAllowed = canUserEdit
                    )
                }
            }
        }
    }
}


/**
 * Composable for Playlist Grid View Card
 * @param modifier Modifier
 * @param coverArt String? - Cover Art URL
 * @param title String - Title of the playlist
 * @param updatedDate String - The date when playlist was last updated
 * @param errorCoverArt Int - Image to display when cover art could not be loaded
 * @param onClickCard () -> Unit - Function to be called when card is clicked
 * @param onDropdownClick (PlaylistDropdownItems) -> Unit - Function to be called when dropdown item is clicked
 * @param isUserSelf Tells whether the current profile belongs to the logged in user
 */
@Composable
fun PlaylistListViewCard(
    modifier: Modifier,
    coverArt: String?,
    title: String,
    updatedDate: String,
    @DrawableRes errorCoverArt: Int = R.drawable.playlist_card_bg1,
    onDropdownClick: (PlaylistDropdownItems) -> Unit,
    onClickCard: () -> Unit,
    canUserEdit: Boolean
) {
    var isDropdownEnabled by remember {
        mutableStateOf(false)
    }
    ListenCardSmall(
        modifier = modifier,
        trackName = "",
        artists = emptyList(),
        coverArtUrl = null,
        goToArtistPage = {},
        coverArt = {
            CoverArtComposable(
                modifier = it.aspectRatio(1f),
                coverArt = coverArt,
                maxGridSize = PlaylistDataRepository.DEFAULT_PLAYLIST_LIST_VIEW_GRID_SIZE,
                errorImage = errorCoverArt,
                areImagesClickable = false
            )
        },
        titleAndSubtitle = {
            Column(modifier = it) {
                Text(
                    text = title,
                    color = ListenBrainzTheme.colorScheme.listenText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ListenBrainzTheme.textStyles.dialogTitleBold.copy(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Updated $updatedDate",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = ListenBrainzTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                )
            }
        },
        onClick = {
            onClickCard()
        },
        dropDown = {
            PlaylistDropdownMenu(
                expanded = isDropdownEnabled,
                onDismiss = { isDropdownEnabled = false },
                onItemClick = {
                    onDropdownClick(it)
                    isDropdownEnabled = false
                },
                isPrivateAllowed = canUserEdit
            )
        },
        onDropdownIconClick = {
            isDropdownEnabled = !isDropdownEnabled
        }
    )
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistGridViewCardPreview() {
    ListenBrainzTheme {
        PlaylistGridViewCard(
            modifier = Modifier,
            coverArt = null,
            title = "Copy of weekly exploration of hemang-mishra",
            updatedDate = "Feb 9",
            onClickCard = { },
            onDropdownClick = { },
            canUserEdit = false
        )
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistListViewCardPreview() {
    ListenBrainzTheme {
        PlaylistListViewCard(
            modifier = Modifier,
            coverArt = null,
            title = "Copy of weekly exploration of hemang-mishra",
            updatedDate = "Feb 9",
            onClickCard = {},
            onDropdownClick = {},
            canUserEdit = false
        )
    }
}