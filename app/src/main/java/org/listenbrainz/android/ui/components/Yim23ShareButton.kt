package org.listenbrainz.android.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

@Composable
fun Yim23ShareButton(viewModel: Yim23ViewModel, typeOfImage: Array<YimShareable>,) {
    var dialogState by remember { mutableStateOf(false) }
    if (dialogState){
        ShowDialog(
            onDismiss = { dialogState = false },
            viewModel = viewModel,
            typeOfImage = typeOfImage
        )
    }
    Button(onClick = {
        dialogState = true
    } , colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)) {
        Icon(imageVector = ImageVector.vectorResource(R.drawable.yim23_share_icon) , contentDescription = "Yim23 share icon" , tint = MaterialTheme.colorScheme.background)
    }
}

@Composable
private fun ShowDialog(
    onDismiss: () -> Unit,
    viewModel: Yim23ViewModel,
    typeOfImage: Array<YimShareable>,
    context: Context = LocalContext.current
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (typeOfImage.size == 2) {
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.saveSharableImage(
                                    sharableType = typeOfImage[1].code,
                                    context = context
                                )
                                onDismiss()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(text = typeOfImage[1].name , style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
                        }
                    }
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.saveSharableImage(
                                sharableType = typeOfImage[0].code,
                                context = context
                            )
                            onDismiss()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = typeOfImage[0].name ,  style = MaterialTheme.typography.bodySmall.copy(fontSize = 20.sp))
                    }
                }

                Row(Modifier.align(Alignment.CenterStart)) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(text = "Cancel" ,  style = MaterialTheme.typography.bodySmall.copy(fontSize = 20.sp))
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
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 20.sp)
            )
        },
        shape = RoundedCornerShape(15.dp),
        backgroundColor = MaterialTheme.colorScheme.onBackground,
        contentColor = MaterialTheme.colorScheme.background,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )


}