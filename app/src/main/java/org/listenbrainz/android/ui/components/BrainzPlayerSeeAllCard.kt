package org.listenbrainz.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun BrainzPlayerSeeAllCard(modifier: Modifier = Modifier,onCardClicked:()->Unit,cardText:String) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(15.dp))
            .size(150.dp)
            .clickable {
                onCardClicked()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ListenBrainzTheme.colorScheme.placeHolderColor)
                .padding(start = 5.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                cardText,
                style = TextStyle(fontSize = 20.sp),
                color = ListenBrainzTheme.colorScheme.lbSignature
            )
        }
    }
}