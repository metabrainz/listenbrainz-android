package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.yimRed

@Preview
@Composable
fun YimEndgameScreen(
    activity: YearInMusicActivity = YearInMusicActivity(),
    paddings: YimPaddings = LocalYimPaddings.current,
    context: Context = LocalContext.current
) {
    
    YearInMusicTheme(redTheme = true) {
        
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(state = rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = paddings.smallPadding)
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        
                YimLabelText(
                    heading = "2022 Releases",
                    subHeading = "Just some of the releases that came out in 2022. Drag, zoom and have fun!"
                )
        
                var zoom by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                var width by remember { mutableStateOf(0f) }
                var height by remember { mutableStateOf(0f) }
        
                Image(
                    painter = painterResource(id = R.drawable.yim_end_collage_large),
                    modifier = Modifier
                        .background(Color.Gray)
                        .clip(RectangleShape)
                        .onPlaced {
                            width = it.size.width.toFloat()
                            height = it.size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures(
                                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                                    val oldScale = zoom
                                    val newScale = zoom * gestureZoom
                                    zoom = newScale.coerceIn(1f, 5f)    // max zoom is 5x
                
                                    val resultOffset =
                                        (offset + centroid / oldScale) - (centroid / newScale + pan / oldScale)
                                    // We need to translate the image in order to make it seem that zoom is done about the centroid.
                
                                    offset = Offset(
                                        resultOffset.x.coerceIn(
                                            0f,
                                            width * (zoom - 1) / newScale       // Limit the max drag we can do.
                                        ),
                                        resultOffset.y.coerceIn(
                                            0f,
                                            height * (zoom - 1) / newScale
                                        )
                                    )
                                }
                            )
                        }
                        .graphicsLayer {
                            translationX =
                                -offset.x * zoom     // Move the image away from origin to make zoom natural
                            translationY = -offset.y * zoom
                            scaleX = zoom
                            scaleY = zoom
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                    contentDescription = null
                )
        
                Spacer(modifier = Modifier.height(paddings.defaultPadding))
        
                Text(
                    text = "Wishing you a wonderful 2023, from the ListenBrainz team.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = paddings.defaultPadding),
                    textAlign = TextAlign.Center
                )
        
                Spacer(modifier = Modifier.height(paddings.defaultPadding))
        
                val uriHandler = LocalUriHandler.current
                val annotatedLinkString: AnnotatedString = buildAnnotatedString {
            
                    val str =
                        "If you have questions or feedback don't hesitate to contact us on our forums, by email, IRC or on twitter."
                    val linkSpanStyle = SpanStyle(
                        color = Color.White,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        textDecoration = TextDecoration.Underline
                    )
            
                    val forumStartIndex = str.indexOf("forums")
                    val forumEndIndex = forumStartIndex + 6
            
                    val emailStartIndex = str.indexOf("email")
                    val emailEndIndex = emailStartIndex + 5
            
                    val ircStartIndex = str.indexOf("IRC")
                    val ircEndIndex = ircStartIndex + 3
            
                    val twitterStartIndex = str.indexOf("twitter")
                    val twitterEndIndex = twitterStartIndex + 7
            
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
            
                }
        
                ClickableText(
                    text = annotatedLinkString,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = paddings.defaultPadding),
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
        
                Spacer(modifier = Modifier.height(paddings.largePadding))
        
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Have a great 2023!", Toast.LENGTH_SHORT).show()
                        activity.finish()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        tint = yimRed,
                        contentDescription = "Navigate to home"
                    )
                }
                
                Spacer(modifier = Modifier.height(paddings.defaultPadding))
        
            }
    
            Text(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = paddings.largePadding, bottom = paddings.tinyPadding),
                text = "With thanks to KC Green for the original 'this is fine' cartoon.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                
            )
        }
    }
}