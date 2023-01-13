package org.listenbrainz.android.presentation.features.yim.screens.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.presentation.features.yim.YimViewModel

@Composable
fun YimShareButton(
    viewModel: YimViewModel,
    typeOfImage: Array<String>,
    modifier: Modifier = Modifier,
    disableButton: Boolean = false
) {
    var dialogState by remember { mutableStateOf(false) }
    if (dialogState){
        ShowDialog(
            onDismiss = { dialogState = false },
            viewModel = viewModel,
            typeOfImage = typeOfImage
        )
    }
    
    IconButton(
        modifier = modifier.size(45.dp),
        onClick = { dialogState = true },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        ),
        enabled = !disableButton
    ) {
        Icon(
            imageVector = Icons.Rounded.Share,
            contentDescription = "Share your Year in Music",
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun ShowDialog(
    onDismiss: () -> Unit,
    viewModel: YimViewModel,
    typeOfImage: Array<String>,
    context: Context = LocalContext.current
){
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (typeOfImage.size == 2) {
                        Button(
                            onClick = {
                                viewModel.saveSharableImage(sharableType = typeOfImage[1], context = context)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(text = displayNameForQuery(typeOfImage[1]))
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.saveSharableImage(sharableType = typeOfImage[0], context = context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = displayNameForQuery(typeOfImage[0]))
                    }
                }
                
                Row(Modifier.align(Alignment.CenterStart)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
            
            
        },
        title = {
            Text(
                text = "Share",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background
            )
        },
        text = {
            Text(
                text = "Choose what you want to share.",
                color = MaterialTheme.colorScheme.background
            )
        },
        shape = RoundedCornerShape(15.dp),
        backgroundColor = MaterialTheme.colorScheme.onBackground,
        contentColor = MaterialTheme.colorScheme.background,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
    
    
}

private fun displayNameForQuery(type: String) : String {
    return when(type){
        "stats" -> "Statistics"
        "artists" -> "Artists"
        "albums" -> "Albums"
        "tracks" -> "Tracks"
        "discovery-playlist" -> "Discoveries"
        "missed-playlist" -> "Missed Tracks"
        else -> ""
    }
}