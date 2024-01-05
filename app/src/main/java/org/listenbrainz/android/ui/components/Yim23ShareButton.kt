package org.listenbrainz.android.ui.components

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import org.listenbrainz.android.R

@Composable
fun Yim23ShareButton() {
    Button(onClick = { /*TODO*/ } , colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)) {
        Icon(imageVector = ImageVector.vectorResource(R.drawable.yim23_share_icon) , contentDescription = "Yim23 share icon" , tint = MaterialTheme.colorScheme.background)
    }
}