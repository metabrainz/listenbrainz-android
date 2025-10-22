package org.listenbrainz.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Utils.Spacer

@Composable
fun MusicBrainzButton(onClick: () -> Unit) {
    ButtonLB(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.musicbrainz_logo),
            contentDescription = "",
            modifier = Modifier.width(20.dp),
            tint = Color.Unspecified
        )

        Spacer(5.dp)

        Text(text = stringResource(R.string.musicbrainz))

        Spacer(5.dp)

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "",
            modifier = Modifier.width(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun MBButtonPreview() {
    ListenBrainzTheme {
        MusicBrainzButton {}
    }
}