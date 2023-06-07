package org.listenbrainz.android.ui.screens.listens

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.listenbrainz.android.R
import org.listenbrainz.android.util.Constants.SPOTIFY_PACKAGE_NAME
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ListeningAppsList(
    viewModel: ListensViewModel = hiltViewModel(),
    onDismiss: () -> Unit
){
    var blacklist by remember { mutableStateOf(viewModel.appPreferences.listeningBlacklist) }
    val isSpotifyLinked by viewModel.isSpotifyLinked.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.fetchLinkedServices()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = MaterialTheme.colorScheme.background,
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ){
                    Text(
                        text = "OK",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        title = {
            Text(
                text = "Listening Apps List",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(items = viewModel.appPreferences.listeningApps){packageName ->
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        
                        // Content
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth(0.85f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
            
                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth(0.15f)
                                        .padding(end = 5.dp),
                                    painter = rememberDrawablePainter(
                                        drawable = viewModel.repository.getPackageIcon(packageName)
                                            ?: AppCompatResources.getDrawable(
                                                context,
                                                R.drawable.music_solid
                                            )
                                    ),
                                    contentDescription = null
                                )
            
                                Text(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    text = viewModel.repository.getPackageLabel(packageName),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                modifier = Modifier
                                    .fillMaxWidth(0.15f)
                                    .align(Alignment.CenterEnd),
                                checked = packageName !in blacklist,
                                onCheckedChange = { isChecked ->
                                    if (!isChecked) {
                                        viewModel.appPreferences.listeningBlacklist =
                                            blacklist + packageName
                                        blacklist = blacklist + packageName
                                    } else {
                                        viewModel.appPreferences.listeningBlacklist =
                                            blacklist - packageName
                                        blacklist = blacklist - packageName
                                    }
                
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.inverseOnSurface,
                                    checkedThumbColor = MaterialTheme.colorScheme.inverseOnSurface
                                )
                            )
                        }
    
                        // Warning for spotify.
                        AnimatedVisibility (visible = packageName == SPOTIFY_PACKAGE_NAME && isSpotifyLinked){
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "Attention",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Spotify is already linked to ListenBrainz.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                            
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun ListeningAppsListPreview(){
    ListeningAppsList {}
}