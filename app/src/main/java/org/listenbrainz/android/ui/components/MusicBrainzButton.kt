package org.listenbrainz.android.ui.components

import androidx.compose.foundation.layout.Row
import org.listenbrainz.shared.resources.musicbrainz
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.musicbrainz_logo
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.new_app_bg_light
import org.listenbrainz.android.util.Utils.Spacer

@Composable
fun MusicBrainzButton(onClick: () -> Unit) {
    ButtonLB(onClick = onClick) {
        Icon(
            painter = painterResource(Res.drawable.musicbrainz_logo),
            contentDescription = "",
            modifier = Modifier.width(20.dp),
            tint = Color.Unspecified
        )

        Spacer(5.dp)

        Text(text = stringResource(Res.string.musicbrainz))

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