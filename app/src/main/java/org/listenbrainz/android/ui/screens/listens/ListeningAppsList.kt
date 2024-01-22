package org.listenbrainz.android.ui.screens.listens

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.screens.settings.PreferencesUiState
import org.listenbrainz.android.util.Constants.SPOTIFY_PACKAGE_NAME

@Composable
fun ListeningAppsList(
    preferencesUiState: PreferencesUiState,
    fetchLinkedServices: () -> Unit,
    getPackageIcon: (String) -> Drawable?,
    getPackageLabel: (String) -> String,
    setWhitelist: (List<String>) -> Unit,
    onDismiss: () -> Unit
){

    LaunchedEffect(Unit){
        fetchLinkedServices()
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
                text = "Listening Apps",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn {
                items(items = preferencesUiState.listeningApps){ packageName ->
                    
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        
                        // Content
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth(0.85f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                val context = LocalContext.current
                                val drawable = getPackageIcon(packageName)
                                    ?: AppCompatResources.getDrawable(context, R.drawable.music_regular)
                                val bitmap = drawable?.toBitmap()
                                val painter = if (bitmap != null) BitmapPainter(bitmap.asImageBitmap()) else null

                                if (painter != null) {
                                    Image(
                                        modifier = Modifier
                                            .fillMaxWidth(0.15f)
                                            .padding(end = 5.dp),
                                        painter = painter,
                                        contentDescription = null
                                    )
                                }

                                Text(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    text = getPackageLabel(packageName),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                modifier = Modifier
                                    .fillMaxWidth(0.15f)
                                    .align(Alignment.CenterEnd),
                                checked = packageName in preferencesUiState.listeningWhitelist,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        setWhitelist(preferencesUiState.listeningWhitelist.toMutableList() + packageName)
                                    } else {
                                        setWhitelist(preferencesUiState.listeningWhitelist.toMutableList() - packageName)
                                    }
                
                                },
                            )
                        }
    
                        // Warning for spotify.
                        AnimatedVisibility (visible = packageName == SPOTIFY_PACKAGE_NAME && preferencesUiState.isSpotifyLinked){
                            
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
    ListeningAppsList(
        PreferencesUiState(),
        {},
        {
            object : Drawable() {
                override fun draw(canvas: Canvas) {
                    TODO("Not yet implemented")
                }
    
                override fun setAlpha(alpha: Int) {
                    TODO("Not yet implemented")
                }
    
                override fun setColorFilter(colorFilter: ColorFilter?) {
                    TODO("Not yet implemented")
                }
    
                override fun getOpacity(): Int {
                    TODO("Not yet implemented")
                }
    
            } },
        { "Package Label" },
        {}
    ){}
}