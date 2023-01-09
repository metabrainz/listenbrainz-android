package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smarttoolfactory.zoom.EnhancedZoomableImage
import com.smarttoolfactory.zoom.rememberEnhancedZoomState
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.screens.components.YimLabelText
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.yimRed


@Composable
fun YimEndgameScreen(
    viewModel: YimViewModel,        // TODO: Remove these
    navController: NavController,
    activity: YearInMusicActivity,
    paddings: YimPaddings = LocalYimPaddings.current,
    context: Context = LocalContext.current
) {
    
    
    
    YearInMusicTheme(redTheme = true) {
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            YimLabelText(heading = "2022 Releases", subHeading = "Just some of the releases that came out in 2022. Drag, zoom and have fun!")
            
            // TODO: mess with this
            /*
            val scaleState = remember { mutableStateOf(1f) }
            val dragState = remember { mutableStateOf( Offset(0f,0f) ) }
            val centroidState = remember { mutableStateOf( Offset(0f,0f) ) }
            val width = 600f
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.yim_end_collage_ninety),
                    modifier = Modifier
                        .requiredWidth(500.dp)
                        .clip(RectangleShape)
                        .pointerInput(Unit) {
                            detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, rotation ->
                                
                                //if (scaleState.value != 1f){
                                scaleState.value *= zoom
                                //}
                                
                                if (dragState.value.x in -(width.pow(scaleState.value))..(width.pow(scaleState.value)) ){
                                    dragState.value += pan
                                }else if (dragState.value.x > width){
                                    dragState.value = Offset(width.pow(scaleState.value),0f)
                                }else{
                                    dragState.value = Offset((-width).pow(scaleState.value),0f)
                                }
                                dragState.value += pan
                                
                            }
                        }
                        .graphicsLayer {
                            scaleX = scaleState.value.coerceIn(1f, 3f)
                            scaleY = scaleState.value.coerceIn(1f, 3f)
                            translationX = dragState.value.x
                            translationY = dragState.value.y
                        },
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }*/
            
            EnhancedZoomableImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                yimRed,
                                Color(0xFFCE32B4),
                                Color(0xFF324CCE),
                                Color(0xFF00BCD4),
                                Color(0xFF4CAF50),
                                Color(0xFFFFEB3B),
                                yimRed,
                            )
                        )
                    ),
                clip = true,
                imageBitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.yim_end_collage_large).asImageBitmap(),
                moveToBounds = true,
                fling = true,
                contentScale = ContentScale.Fit,
                enhancedZoomState = rememberEnhancedZoomState(imageSize = IntSize(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500f, LocalContext.current.resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, LocalContext.current.resources.displayMetrics).toInt()),
                    limitPan = true
                )
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
        
                val str = "If you have questions or feedback don't hesitate to contact us on our forums, by email, IRC or on twitter."
                val linkSpanStyle = SpanStyle(
                    color = Color.White,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    textDecoration = TextDecoration.Underline
                )
                
                val forumStartIndex = str.indexOf("forums")     // TODO: Inline these once reviewed
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
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = paddings.defaultPadding),
                onClick = { offset ->
                    annotatedLinkString
                        .getStringAnnotations(offset, offset)
                        .firstOrNull()?.let { stringAnnotation ->
                            if (stringAnnotation.tag == "email"){
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, stringAnnotation.item)
                                    putExtra(Intent.EXTRA_SUBJECT, "ListenBrainz App Review")
                                    // putExtra(Intent.EXTRA_TEXT, "Email body")
                                    // putExtra(Intent.EXTRA_STREAM, attachment)
                                }
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            }else {
                                uriHandler.openUri(stringAnnotation.item)
                            }
                        }
                }
            )
            
            Spacer(modifier = Modifier.height(paddings.defaultPadding))
            
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
            
            Spacer(modifier = Modifier.height(paddings.largePadding))
            
            Text(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = paddings.defaultPadding, bottom = paddings.tinyPadding),
                text = "With thanks to KC Green for the original 'this is fine' cartoon.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
            
        }
    }
}