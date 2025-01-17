package org.listenbrainz.android.ui.screens.profile.createdforyou

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple


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
    title: String,
    fractionLeft: Float = 0.0f,
    modifier: Modifier = Modifier,
    height: Int = 140,
    width: Int = 140,
    sizeIncrementOnSelect: Int = 20,
    alignment: Alignment = Alignment.TopStart,
    cardBg: Int = R.drawable.playlist_card_bg1,
    isSelected: Boolean = false,
    onSaveClick: () -> Unit,
    onPlaylistSelect: () -> Unit
) {
    val actualHeight = if (isSelected) height + sizeIncrementOnSelect else height
    val actualWidth = if (isSelected) width + sizeIncrementOnSelect else width
    Box(
        modifier = modifier
            .height(actualHeight.dp)
            .width(actualWidth.dp)
            .clip(shape = ListenBrainzTheme.shapes.listenCardSmall)
            .border(
                if (isSelected) 2.dp else 0.dp,
                lb_purple,
                shape = ListenBrainzTheme.shapes.listenCardSmall
            )
            .animateContentSize(),
        contentAlignment = alignment
    ) {
        Box(modifier = Modifier
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
            color = if (fractionLeft > thresholdOfColorChange) initialProgressColor else endProgressColor,
            startAngle = startAnge,
            sweepAngle = sweepAngle,
            useCenter = true,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PlaylistTitleCardPreview() {
    PlaylistTitleCard(
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