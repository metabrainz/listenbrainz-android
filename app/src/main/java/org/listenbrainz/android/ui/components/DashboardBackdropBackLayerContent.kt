package org.listenbrainz.android.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.newsbrainz.NewsBrainzActivity
import org.listenbrainz.android.ui.screens.yim.YearInMusicActivity
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple

@Composable
fun BackLayerContent(activity: Activity) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            Image(
                modifier = Modifier
                    .size(230.dp, 230.dp)
                    .padding(20.dp),
                painter = painterResource(id = R.drawable.ic_listenbrainz_logo_no_text),
                contentDescription = "ListenBrainz",
                contentScale = ContentScale.Fit
            )
        
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = lb_purple
                            )
                    ) {
                        append("Listen")
                    }
                
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = lb_orange
                            )
                    ) {
                        append("Brainz")
                    }
                },
                fontSize = 45.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Yim Card
            HomeScreenCard(
                activity = activity,
                nextActivity = YearInMusicActivity(),
                iconId = R.drawable.yim_radio,
                title = "Year in Music",
                subTitle = "Your Whole Year Summarized"
            )
            
            // NewsBrainz Card
            HomeScreenCard(
                activity = activity,
                nextActivity = NewsBrainzActivity(),
                iconId = R.drawable.ic_news,
                title = "News",
                subTitle = stringResource(id = R.string.news_card)
            )
            
        }
    }
}

@Composable
private fun HomeScreenCard(
    activity: Activity,
    nextActivity: Activity,
    iconId: Int,
    title: String,
    subTitle: String,
    context: Context = LocalContext.current
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                activity.startActivity(
                    Intent(
                        context,
                        nextActivity::class.java
                    )
                )
            },
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                painter = painterResource(id = iconId),
                alignment = Alignment.CenterStart,
                contentDescription = "",
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = title,
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subTitle,
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}
