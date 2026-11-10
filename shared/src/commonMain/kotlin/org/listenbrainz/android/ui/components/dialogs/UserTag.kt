package org.listenbrainz.android.ui.components.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun UserTag(
    user: String,
    modifier: Modifier = Modifier,
    showCrossButton: Boolean = false,
    onCrossButtonClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable {
            if (showCrossButton) {
                onCrossButtonClick()
            }
        },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = ListenBrainzTheme.colorScheme.lbSignature
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Text(
                modifier = Modifier.padding(vertical = ListenBrainzTheme.paddings.insideCard),
                text = user,
                style = ListenBrainzTheme.textStyles.feedBlurbContent,
                color = ListenBrainzTheme.colorScheme.onLbSignature
            )
            
            if (showCrossButton){
                Icon(
                    modifier = Modifier.height(14.dp).padding(start = 4.dp, end = 6.dp),
                    imageVector = Icons.Default.Cancel,
                    tint = ListenBrainzTheme.colorScheme.onLbSignature,
                    contentDescription = "Remove user"
                )
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Preview
@Composable
fun UserTagPreview() {
    ListenBrainzTheme {
        UserTag(user = "Jasjeet", showCrossButton = true)
    }
}