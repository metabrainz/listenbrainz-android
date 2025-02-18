package org.listenbrainz.android.ui.screens.profile.playlists

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.CoverArtComposable
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PlaylistGridViewCard(
    modifier: Modifier,
    coverArt: String?,
    title: String,
    trackCount: Int,
    updatedDate: String,
    @DrawableRes errorCoverArt: Int = R.drawable.playlist_card_bg1,
    onClickOptionsButton:()->Unit,
    onClickCard: ()->Unit
) {
    Card(
        onClick = onClickCard,
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        modifier = modifier
            .padding(8.dp)
            .width(150.dp)
            .clip(ListenBrainzTheme.shapes.listenCardSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // Image (Coil for async loading)
            CoverArtComposable(
                modifier = Modifier,
                coverArt = coverArt,
                gridSize = 3,
                areImagesClickable = true
            )
//            SvgWithWebView(
//                svgContent = sample,
//                width = 150.dp,
//                height = 150.dp
//            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Column(
                    modifier = Modifier.padding(8.dp)
                        .weight(1.0f)
                ) {
                    Text(
                        text = title,
                        color = ListenBrainzTheme.colorScheme.listenText,
                        style = ListenBrainzTheme.textStyles.dialogTitleBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "$trackCount tracks",
                            fontSize = 14.sp,
                            color = ListenBrainzTheme.colorScheme.onBackground
                        )
                        Text(
                            text = " | Updated $updatedDate",
                            fontSize = 14.sp,
                            color = ListenBrainzTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                        )
                    }
                }
                IconButton(
                    onClick = {
                        onClickOptionsButton()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_options),
                        contentDescription = "Options button"
                    )
                }
            }
        }
    }
}


@Composable
fun PlaylistListViewCard(){

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
            trackCount = 10,
            updatedDate = "2 days ago",
            onClickOptionsButton = {}
        ){}
    }
}

val sample = "<svg version=\"1.1\"\n" +
        "     xmlns=\"http://www.w3.org/2000/svg\"\n" +
        "     xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
        "     role=\"img\"\n" +
        "     viewBox=\"0 0 500 500\"\n" +
        "     width=\"500\"\n" +
        "     height=\"500\">\n" +
        "\n" +
        "     \n" +
        "          <title>Claypool Hour</title>\n" +
        "          <desc>Nothing but Les Claypool, put it on a loop</desc>\n" +
        "     \n" +
        "\n" +
        "     <rect id=\"background\" fill=\"#FFFFFF\" x=\"0\" ry=\"0\" width=\"500\" height=\"500\"/>\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/c0c7ae45-1854-432b-8cd1-7cd606cd9d2b\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"0\"\n" +
        "        y=\"0\"\n" +
        "        width=\"166\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-3330b3f4-06ff-3beb-9d89-d40294d8563b/mbid-3330b3f4-06ff-3beb-9d89-d40294d8563b-27029909293_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>John the Fisherman - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/3aa89da7-210a-409a-a5da-7634e27c5d0b\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"166\"\n" +
        "        y=\"0\"\n" +
        "        width=\"166\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-8fc943bf-5fd2-40ba-88d0-f3d0516208de/mbid-8fc943bf-5fd2-40ba-88d0-f3d0516208de-15297952130_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Pressman - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/5010da62-0711-478f-866a-9bbd09771098\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"332\"\n" +
        "        y=\"0\"\n" +
        "        width=\"168\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-c3814cca-63d1-4cfa-9934-60957205b86b/mbid-c3814cca-63d1-4cfa-9934-60957205b86b-26730700764_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Tommy the Cat - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/64966f99-8d2a-43b6-b743-fafc8dd1eb8e\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"0\"\n" +
        "        y=\"166\"\n" +
        "        width=\"166\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-66f56d73-6e8f-4adc-8df0-159df40d168d/mbid-66f56d73-6e8f-4adc-8df0-159df40d168d-14744525854_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Prelude to a Crawl - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/785a34c0-66fe-4b5b-851c-c5a89533bf48\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"166\"\n" +
        "        y=\"166\"\n" +
        "        width=\"166\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-ebcd119e-73be-35bb-8f1a-a506aa73b95d/mbid-ebcd119e-73be-35bb-8f1a-a506aa73b95d-28985005149_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>My Name Is Mud - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/de899a64-3574-416e-9093-455c6043f4bb\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"332\"\n" +
        "        y=\"166\"\n" +
        "        width=\"168\"\n" +
        "        height=\"166\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-62a84b82-4822-44c5-a394-1ee151e7a34d/mbid-62a84b82-4822-44c5-a394-1ee151e7a34d-11600669018_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Over the Electric Grapevine - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/104bee4d-6cb4-4f8a-9f51-9207b4f61cd0\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"0\"\n" +
        "        y=\"332\"\n" +
        "        width=\"166\"\n" +
        "        height=\"168\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-03d72a1a-870d-4156-abc2-1c8a3cd962fc/mbid-03d72a1a-870d-4156-abc2-1c8a3cd962fc-7956214318_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Over the Falls - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/92405746-7300-40ba-86a5-e9edd4bd7b72\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"166\"\n" +
        "        y=\"332\"\n" +
        "        width=\"166\"\n" +
        "        height=\"168\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-aa46039d-383c-4796-bb46-891f8cdd449f/mbid-aa46039d-383c-4796-bb46-891f8cdd449f-15297950595_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Shake Hands With Beef (extended version) - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "          \n" +
        "  \n" +
        "  <a href=\"https://listenbrainz.org/release/0af03797-1cf9-4840-8fbd-1f04f87b9eb1\" target=\"_blank\">\n" +
        "  \n" +
        "    <image\n" +
        "        x=\"332\"\n" +
        "        y=\"332\"\n" +
        "        width=\"168\"\n" +
        "        height=\"168\"\n" +
        "        preserveAspectRatio=\"xMidYMid slice\"\n" +
        "        href=\"https://archive.org/download/mbid-7fe0e4fe-7960-3711-88aa-b5503678d8ba/mbid-7fe0e4fe-7960-3711-88aa-b5503678d8ba-988889092_thumb500.jpg\">\n" +
        "        \n" +
        "        <title>Coattails of a Dead Man - Primus</title>\n" +
        "        \n" +
        "    </image>\n" +
        "  \n" +
        "  </a>\n" +
        "  \n" +
        "\n" +
        "     \n" +
        "</svg>"