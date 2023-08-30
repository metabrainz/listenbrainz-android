package org.listenbrainz.android.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.viewmodel.YimViewModel

@Composable
fun YimShareButton(
    viewModel: YimViewModel,
    typeOfImage: Array<YimShareable>,
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
        modifier = modifier
            .size(50.dp)
            .testTag(stringResource(id = R.string.tt_yim_share))
            .clip(CircleShape),
        onClick = { dialogState = true },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        ),
        enabled = !disableButton
    ) {
        Image (
            imageVector = Icons.Rounded.Share,
            contentDescription = "Share your Year in Music",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ShowDialog(
    onDismiss: () -> Unit,
    viewModel: YimViewModel,
    typeOfImage: Array<YimShareable>,
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
                                viewModel.saveSharableImage(sharableType = typeOfImage[1].code, context = context)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(text = typeOfImage[1].name)
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.saveSharableImage(sharableType = typeOfImage[0].code, context = context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = typeOfImage[0].name)
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