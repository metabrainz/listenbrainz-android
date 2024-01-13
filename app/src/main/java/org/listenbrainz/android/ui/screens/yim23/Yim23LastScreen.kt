package org.listenbrainz.android.ui.screens.yim23

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23ThemeData
import org.listenbrainz.android.model.yimdata.Yim23TopDiscoveries
import org.listenbrainz.android.model.yimdata.Yim23Track
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23LastScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceBetween) {
            Box (modifier = Modifier.padding(top = 10.dp)) {
                Column {
                    Row (modifier = Modifier.fillMaxWidth() ,
                        horizontalArrangement = Arrangement.Center ,
                        verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick =
                        {
                            navController.navigate(route = Yim23Screens.YimLandingScreen.name)
                        } ,
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onBackground)) {
                            Text("START AGAIN" , style = MaterialTheme.typography.bodySmall ,
                                color = MaterialTheme.colorScheme.background)
                        }
                        Spacer(modifier = Modifier.padding(start = 10.dp , end = 10.dp))
                        IconButton(onClick = { navController.popBackStack() } , modifier = Modifier
                            .clip(
                                RoundedCornerShape(100.dp)
                            )
                            .size(26.dp)
                            .background(MaterialTheme.colorScheme.onBackground)
                        ) {
                            Image(imageVector = ImageVector.vectorResource(R.drawable.yim23_up_arrow),
                                contentDescription = "Up arrow" , colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.background))
                        }
                    }
                    Spacer(modifier = Modifier.padding(top = 5.dp , bottom = 5.dp))
                    Row (modifier = Modifier.fillMaxWidth() ,
                        horizontalArrangement = Arrangement.Center) {
                        Text("#YEAR IN MUSIC" , color = MaterialTheme.colorScheme.onBackground ,
                            style = MaterialTheme.typography.titleLarge)
                    }
                }

            }
            
            Box () {
                Column (modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 11.dp, end = 11.dp) ,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = viewModel.themeType.value.heartRes),
                        contentDescription = "Heart illustration" , modifier = Modifier
                            .height(123.dp)
                            .width(108.dp))
                    Text("Wishing you a restful 2024,from the ListenBrainz team." ,
                        color = MaterialTheme.colorScheme.onBackground ,
                        style = MaterialTheme.typography.bodyMedium , textAlign = TextAlign.Center)

                    val uriHandler = LocalUriHandler.current
                    val context = LocalContext.current
                    val annotatedLinkString: AnnotatedString = buildAnnotatedString {

                        val str =
                            "If you have questions or feedback don't hesitate to contact us on our forums, by email, IRC, X, Bluesky or Mastodon\n"
                        val linkSpanStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.onBackground ,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize ,
                            textDecoration = TextDecoration.Underline ,
                        )

                        val forumStartIndex = str.indexOf("our")
                        val forumEndIndex = forumStartIndex + 10

                        val emailStartIndex = str.indexOf("email")
                        val emailEndIndex = emailStartIndex + 5

                        val ircStartIndex = str.indexOf("IRC")
                        val ircEndIndex = ircStartIndex + 3

                        val twitterStartIndex = str.indexOf("X")
                        val twitterEndIndex = twitterStartIndex + 1

                        val mastodonStartIndex = str.indexOf("Mastodon")
                        val mastodonEndIndex = mastodonStartIndex + 8

                        val blueskyStartIndex = str.indexOf("Bluesky")
                        val blueskyEndIndex = blueskyStartIndex + 7


                        append(str)



                        addStyle(
                            style = linkSpanStyle, start = forumStartIndex, end = forumEndIndex
                        )

                        addStyle(
                            style = linkSpanStyle, start = emailStartIndex, end = emailEndIndex
                        )

                        addStyle(
                            style = linkSpanStyle, start = ircStartIndex, end = ircEndIndex
                        )

                        addStyle(
                            style = linkSpanStyle, start = twitterStartIndex, end = twitterEndIndex
                        )

                        addStyle(
                            style = linkSpanStyle, start = mastodonStartIndex, end = mastodonEndIndex
                        )

                        addStyle(
                            style = linkSpanStyle, start = blueskyStartIndex, end = blueskyEndIndex
                        )

                        // attach a string annotation that stores a URL to the text "link"
                        addStringAnnotation(
                            tag = "forum",
                            annotation = "https://community.metabrainz.org/c/listenbrainz/18",
                            start = forumStartIndex,
                            end = forumEndIndex
                        )
                        addStringAnnotation(
                            tag = "email",
                            annotation = "listenbrainz@metabrainz.org",
                            start = emailStartIndex,
                            end = emailEndIndex
                        )
                        addStringAnnotation(
                            tag = "irc",
                            annotation = "https://web.libera.chat/#metabrainz",
                            start = ircStartIndex,
                            end = ircEndIndex
                        )
                        addStringAnnotation(
                            tag = "twitter",
                            annotation = "https://twitter.com/listenbrainz",
                            start = twitterStartIndex,
                            end = twitterEndIndex
                        )
                        addStringAnnotation(
                            tag = "Mastodon",
                            annotation = "https://mastodon.social/@metabrainz",
                            start = mastodonStartIndex,
                            end = mastodonEndIndex
                        )
                        addStringAnnotation(
                            tag = "Bluesky",
                            annotation = "https://bsky.app/profile/metabrainz.bsky.social",
                            start = blueskyStartIndex,
                            end = blueskyEndIndex
                        )
                    }

                    ClickableText(
                        text = annotatedLinkString,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground , textAlign = TextAlign.Center),
                        onClick = { charOffset ->
                            annotatedLinkString
                                .getStringAnnotations(charOffset, charOffset)
                                .firstOrNull()?.let { stringAnnotation ->
                                    if (stringAnnotation.tag == "email") {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:")
                                            putExtra(Intent.EXTRA_EMAIL, arrayOf(stringAnnotation.item))
                                            putExtra(Intent.EXTRA_SUBJECT, "ListenBrainz App Review")
                                            // putExtra(Intent.EXTRA_TEXT, "Email body")
                                            // putExtra(Intent.EXTRA_STREAM, attachment)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                "Send Email"
                                            )
                                        )
                                    } else {
                                        uriHandler.openUri(stringAnnotation.item)
                                    }
                                }
                        }
                    )

                }
            }

            Box (modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.onBackground) , contentAlignment = Alignment.Center) {
                Column {
                    Column (horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material.Text(
                            username.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.paddingFromBaseline(top = 40.dp)
                        )
                        Row (modifier = Modifier.fillMaxWidth() ,
                            verticalAlignment = Alignment.CenterVertically ,
                            horizontalArrangement = Arrangement.Center) {
                            Yim23ShareButton(viewModel=viewModel , typeOfImage = arrayOf(YimShareable.OVERVIEW))
//                            ListenBrainzProfileButton(navController = navController)
                        }
                    }
                }
            }
        }
    }
}