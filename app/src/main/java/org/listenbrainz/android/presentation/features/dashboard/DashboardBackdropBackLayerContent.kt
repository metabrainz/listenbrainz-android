package org.listenbrainz.android.presentation.features.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.newsbrainz.NewsBrainzActivity
import org.listenbrainz.android.presentation.theme.lb_orange
import org.listenbrainz.android.presentation.theme.lb_purple


@Composable
fun BackLayerContent(activity: Activity, applicationContext: Context) {
    Surface(modifier = Modifier
        .fillMaxSize()          // Fixed Back Layer Content not taking full size.
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)   // Only Change here.
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
<<<<<<< HEAD
                            color = lb_purple
=======
                            color = colorResource(
                                id = R.color.lb_purple
                            )
>>>>>>> a5cc5b7e2229110d6fe69b45aaa30bef5a372684
                        )
                    ) {
                        append("Listen")
                    }
                
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
<<<<<<< HEAD
                            color = lb_orange
=======
                            color = colorResource(
                                id = R.color.lb_orange
                            )
>>>>>>> a5cc5b7e2229110d6fe69b45aaa30bef5a372684
                        )
                    ) {
                        append("Brainz")
                    }
                },
                fontSize = 45.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
        
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = {
                        activity.startActivity(
                            Intent(
                                applicationContext,
                                NewsBrainzActivity::class.java
                            )
                        )
                    }),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.onSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Image(
                        modifier = Modifier
                            .size(80.dp, 80.dp)
                            .padding(4.dp),
                        painter = painterResource(id = R.drawable.ic_news),
                        alignment = Alignment.CenterStart,
                        contentDescription = "",
                        contentScale = ContentScale.Fit
                    )
                
                    Spacer(modifier = Modifier.width(16.dp))
                
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            text = "News",
                            modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                            color = MaterialTheme.colors.surface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.subtitle1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.news_card),
                            modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                            color = MaterialTheme.colors.surface,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}
