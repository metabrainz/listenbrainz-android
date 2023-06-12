package org.listenbrainz.android.ui.screens.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.util.Utils.sendFeedback

@Composable
fun AboutScreen(version: String, onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "About", color = MaterialTheme.colorScheme.onSurface) },
                backgroundColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, FeaturesActivity::class.java))
                    }) {
                        Image(painter = painterResource(id = R.drawable.ic_waving_hand), contentDescription = "Features")
                    }
                    IconButton(onClick = {  sendFeedback(context) }) {
                        Image(painter = painterResource(id = R.drawable.ic_feedback), contentDescription = "Feedback")
                    }
                }
            )
        },
        content = {
            Column(  modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_listenbrainz_logo),
                    contentDescription = "ListenBrainz Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(10.dp)
                )

                Text(
                    text = "Version $version",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("ListenBrainz keeps track of music you listen to and provides you with insights into your listening habits. We're completely open-source and publish our data as open data.",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("What does it do?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(0.dp, 8.dp, 8.dp, 8.dp)
                )

                Text("ListenBrainz is operated by the MetaBrainz Foundation which has a long-standing history of curating, protecting and making music data available to the public. MetaBrainz commits itself to safe-guarding the ListenBrainz data indefinitely.",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("Development",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(0.dp, 8.dp, 8.dp, 8.dp)
                )

                Text(
                    "The source code for this app is available on GitHub at https://github.com/metabrainz/listenbrainz-android\n\n" +
                            "Got something interesting you'd like to ask or share? Start a discussion at #metabrainz IRC channel on libera.chat.\n\n" +
                            "Reports, comments and suggestions are welcomed, just tap 'Feedback' in the menu to send us an email!",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("Bugs",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(0.dp, 8.dp, 8.dp, 8.dp)
                )

                Text("You should be aware that error logs are sent from this app in the event of a crash. Personally identifiable information is not included. You can disable this in the settings menu if you like.",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text("Attributions",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(0.dp, 8.dp, 8.dp, 8.dp)
                )

                Text(
                    "This app uses\n\n" +
                            "Icons designed by Freepik, Pixel Perfect, Good Ware, photo3idea_studio and Pixelmeetup from www.flaticon.com and www.freepik.com\n\n" +
                            "Animations by Korhan Ulusoy, Jake Cowan, KidA Studio, puput Santoso and Paul Roux on LottieFiles from lottiefiles.com\n\n" +
                            "The complete resources with links can be found at\n" +
                            "https://github.com/metabrainz/listenbrainz-android/blob/master/app/src/main/play/asset_attributions.md",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
