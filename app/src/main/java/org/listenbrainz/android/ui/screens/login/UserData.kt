package org.listenbrainz.android.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun UserData(viewModel: ListensViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(180.dp)
            .clickable(onClick = {
                //onItemClicked(listen)
            }),
        elevation = 0.dp,
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = viewModel.appPreferences.username!!,
                modifier = Modifier.padding(4.dp),
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 40.dp)
        ) {
            Text(
                text = "abc",
                modifier = Modifier.padding(4.dp),
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = "abc",

                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = if (onScreenUiModeIsDark()) Color.White else lb_purple,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "abc",
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = if (onScreenUiModeIsDark()) Color.White else lb_purple.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.caption,
                    maxLines = 2
                )
            }
        }
    }
}

@Preview
@Composable
fun UserDataPreview() {
    UserData(viewModel = hiltViewModel())
}