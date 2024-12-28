package org.listenbrainz.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.new_app_bg_light

@Composable
fun MusicBrainzButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick, modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF353070))
            .width(140.dp)
            .height(30.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.musicbrainz_logo),
                contentDescription = "",
                modifier = Modifier
                    .width(20.dp)
                    .height(30.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                "MusicBrainz",
                color = new_app_bg_light,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(5.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "",
                tint = new_app_bg_light,
                modifier = Modifier
                    .width(30.dp)
                    .height(20.dp)
            )
        }
    }
}