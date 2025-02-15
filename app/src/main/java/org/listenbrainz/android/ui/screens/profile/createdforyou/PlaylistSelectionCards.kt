package org.listenbrainz.android.ui.screens.profile.createdforyou

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.createdForYou.AdditionalMetadata
import org.listenbrainz.android.model.createdForYou.UserPlaylistExtensionData
import org.listenbrainz.android.model.createdForYou.UserPlaylist
import org.listenbrainz.android.model.createdForYou.Extension
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * A row of cards that display the title of created for you playlists.
 * [selectedPlaylist] is the selected playlist.
 * [playlists] is the list of playlists.
 * [onSaveClick] is the action to be performed when the save button is clicked.
 * [onPlaylistSelect] is the action to be performed when the card is selected.
 */
@Composable
fun PlaylistSelectionCardRow(
    modifier: Modifier,
    selectedPlaylist: UserPlaylist? = null,
    playlists: List<UserPlaylist>,
    onSaveClick: (UserPlaylist) -> Unit,
    onPlaylistSelect: (UserPlaylist) -> Unit
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(playlists) { index, playlist->
            if (index == 0) {
                Spacer(modifier = Modifier.width(8.dp))
            }

            PlaylistTitleCard(
                modifier = Modifier.size(140.dp),
                title = remember(playlist) {
                    modifyTitle(playlist)
                },
                fractionLeft = remember(playlist) {
                    getFractionLeft(
                        playlist.date,
                        playlist.extension.createdForYouExtensionData.additionalMetadata.expiresAt
                    )
                },
                isSelected = selectedPlaylist == playlist,
                cardBg = remember(index) { getCardBg(index) },
                alignment = Alignment.Center,
                onSaveClick = { onSaveClick(playlist) },
                onPlaylistSelect = { onPlaylistSelect(playlist) }
            )

            if (index == playlists.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}


/**
 * A card that displays the title of a playlist.
 * [title] is the title of the playlist.
 * [fractionLeft] is the percentage of the task left to be completed.
 * [modifier] is the modifier for the card.
 * [height] is the height of the card.
 * [width] is the width of the card.
 * [sizeIncrementOnSelect] is the increment in size when the card is selected.
 * [alignment] is the alignment of the title.
 * [cardBg] is the background of the card.
 * [isSelected] is the state of the card.
 * [onSaveClick] is the action to be performed when the save button is clicked.
 * [onPlaylistSelect] is the action to be performed when the card is selected.
 */
@Composable
fun PlaylistTitleCard(
    modifier: Modifier = Modifier,
    title: String,
    fractionLeft: Float = 0.0f,
    sizeIncrementOnSelect: Int = 10,
    alignment: Alignment = Alignment.TopStart,
    cardBg: Int = R.drawable.playlist_card_bg1,
    isSelected: Boolean = false,
    onSaveClick: () -> Unit,
    onPlaylistSelect: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape = ListenBrainzTheme.shapes.listenCardSmall)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = ListenBrainzTheme.colorScheme.lbSignature,
                shape = ListenBrainzTheme.shapes.listenCardSmall
            )
            .animateContentSize(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onPlaylistSelect() }
        ) {
            Image(
                painter = painterResource(cardBg),
                contentDescription = "Playlist Card Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = ListenBrainzTheme.textStyles.dialogTitleBold,
                color = lb_purple,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = ListenBrainzTheme.paddings.defaultPadding,
                        start = ListenBrainzTheme.paddings.insideCard,
                        end = ListenBrainzTheme.paddings.insideCard
                    )
            )
            ProgressCircle(
                fractionLeft = fractionLeft,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        end = ListenBrainzTheme.paddings.insideCard,
                        top = ListenBrainzTheme.paddings.insideCard
                    )
                    .alpha(0.4f),
                size = 32
            )
        }

        Image(
            painter = painterResource(R.drawable.playlist_save),
            contentDescription = "Button to save the playlist",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    top = ListenBrainzTheme.paddings.insideCard,
                    start = ListenBrainzTheme.paddings.insideCard
                )
                .clickable { onSaveClick() }
        )

    }
}

/**
 * A circular progress indicator that shows the progress of a task.
 * [fractionLeft] is the percentage of the task left to be completed.
 * Color changes after a certain percentage of completion.
 */
@Composable
fun ProgressCircle(
    fractionLeft: Float = 1.0f,
    modifier: Modifier = Modifier,
    size: Int,
    initialProgressColor: Color = Color.White,
    endProgressColor: Color = Color.Red,
    thresholdOfColorChange: Float = 0.3f
) {
    Canvas(
        modifier.size(size.dp)
    ) {
        val startAnge = 270f
        val sweepAngle = -360 * fractionLeft
        drawArc(
            color = Color.White.copy(alpha = 0.3f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
        )
        drawArc(
            color = if (fractionLeft > thresholdOfColorChange) initialProgressColor else endProgressColor,
            startAngle = startAnge,
            sweepAngle = sweepAngle,
            useCenter = true,
        )
    }
}

//This function provides a background for the card based on the index.
fun getCardBg(index: Int): Int{
    return when(index%3){
        0 -> R.drawable.playlist_card_bg1
        1 -> R.drawable.playlist_card_bg2
        2 -> R.drawable.playlist_card_bg3
        else -> R.drawable.playlist_card_bg1
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistTitleCardPreview() {
    PlaylistTitleCard(
        modifier = Modifier.size(140.dp),
        title = "Last Week's Exploration",
        fractionLeft = 0.4f,
        alignment = Alignment.Center,
        onSaveClick = {},
        onPlaylistSelect = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ProgressCirclePreview() {
    ProgressCircle(fractionLeft = 0.3f, size = 40)
}

@Preview(showBackground = true, uiMode =  UI_MODE_NIGHT_YES)
@Composable
fun PlaylistTitleCardRowPreview() {
    var selectedPlaylist by remember { mutableStateOf<UserPlaylist?>(null) }
    ListenBrainzTheme {
        val playlist1 = UserPlaylist(
            title = "Weekly Exploration for hemang-mishra, week of 2025-01-13 Mon",
            date = "2025-01-13T00:07:44.741098+00:00",
            extension = Extension(
                createdForYouExtensionData = UserPlaylistExtensionData(
                    additionalMetadata = AdditionalMetadata(
                        expiresAt = "2025-01-27T00:07:41.737783"
                    )
                )
            )
        )
        val playlist2 = UserPlaylist(
            title = "Weekly Exploration for hemang-mishra, week of 2025-01-06 Mon",
            date = "2025-01-06T00:14:18.151711+00:00",
            extension = Extension(
                createdForYouExtensionData = UserPlaylistExtensionData(
                    additionalMetadata = AdditionalMetadata(
                        expiresAt = "2025-01-20T00:14:15.895660"
                    )
                )
            )
        )
        PlaylistSelectionCardRow(
            modifier = Modifier.padding(16.dp),
            playlists = listOf(
                playlist1,
                playlist2,
                UserPlaylist(title = "Last Week's Exploration")
            ),
            selectedPlaylist = selectedPlaylist,
            onSaveClick = {},
            onPlaylistSelect = {
                selectedPlaylist = it
            }
        )
    }
}

//This function provides a shorter title to created for playlists.
fun modifyTitle(createdForYouPlaylist: UserPlaylist): String{
    if(createdForYouPlaylist.title == null) return "No title"
    if(!createdForYouPlaylist.title.contains("Weekly Exploration")) return createdForYouPlaylist.title
    val millis = convertISOTimeStampToMillis(createdForYouPlaylist.date ?: "")
    val currentTime = System.currentTimeMillis()
    if(currentTime - millis < 7*24*60*60*1000) return "Weekly Exploration"
    return "Last Week's Exploration"
}

//This function converts an ISO timestamp to milliseconds.
fun convertISOTimeStampToMillis(timeStamp: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = sdf.parse(timeStamp)
    return date?.time ?: 0L
}

//This function calculates the fraction of time left for a task to be completed.
fun getFractionLeft(createdTimeStamp: String?, expiryTimeStamp: String?): Float {
    if (createdTimeStamp == null || expiryTimeStamp == null) return 0.0f
    val currentTime = System.currentTimeMillis()
    val createdTime = convertISOTimeStampToMillis(createdTimeStamp)
    val expiryTime = convertISOTimeStampToMillis(expiryTimeStamp)
    val fraction =  (expiryTime - currentTime).toFloat() / (expiryTime - createdTime).toFloat()
    return if (fraction < 0 || fraction > 1.0f) 0.0f else fraction
}