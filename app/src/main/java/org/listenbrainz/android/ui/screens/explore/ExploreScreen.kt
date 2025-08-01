package org.listenbrainz.android.ui.screens.explore

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.newsbrainz.NewsBrainzActivity
import org.listenbrainz.android.ui.screens.yim.YearInMusicActivity
import org.listenbrainz.android.ui.screens.yim23.YearInMusic23Activity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun ExploreScreen(goToHueSoundScreen: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ListenBrainzTheme.colorScheme.background
    ) {

        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //HueSound
            item {
                ExploreScreenCard(
                    nextActivity = null,
                    iconId = R.drawable.huesound_cover,
                    title = "HueSound",
                    subTitle = stringResource(id = R.string.discover),
                    nextScreen = goToHueSoundScreen
                )
            }

            item {
                ExploreScreenCard(
                    nextActivity = YearInMusic23Activity::class.java,
                    iconId = R.drawable.yim23_explore_tile,
                    title = "Your Year in Music 2023",
                    subTitle = "Review"
                )
            }
            item {
                ExploreScreenCard(
                    nextActivity = YearInMusicActivity::class.java,
                    iconId = R.drawable.yim2022,
                    title = "Your Year in Music 2022",
                    subTitle = "Review"
                )
            }
            // Yim Card

            item {
                ExploreScreenCard(
                    nextActivity = NewsBrainzActivity::class.java,
                    iconId = R.drawable.all_projects,
                    title = "News",
                    subTitle = stringResource(id = R.string.news_card)
                )
            }
            // NewsBrainz Card


        }
    }
}

@Composable
private fun ExploreScreenCard(
    nextActivity: Class<out ComponentActivity>?,
    iconId: Int,
    title: String,
    subTitle: String,
    context: Context = LocalContext.current,
    nextScreen: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = 10.dp
            )
            .clickable {
                if (nextActivity != null)
                    context.startActivity(
                        Intent(
                            context,
                            nextActivity
                        )
                    )
                else {
                    nextScreen()
                }
            },
        color = ListenBrainzTheme.colorScheme.level1,
        elevation = 6.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = iconId),
                alignment = Alignment.Center,
                contentDescription = "",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = ListenBrainzTheme.paddings.horizontal),
                color = ListenBrainzTheme.colorScheme.listenText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = ListenBrainzTheme.paddings.horizontal),
                color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                style = MaterialTheme.typography.caption
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview
@Composable
fun ExplorePreview() {
    ListenBrainzTheme {
        ExploreScreen()
    }
}
