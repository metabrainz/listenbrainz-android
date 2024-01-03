package org.listenbrainz.android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens


@Composable
fun Yim23Header (username : String , navController : NavController) {
    Box () {
        Row (modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 20.dp)
            .fillMaxWidth()
            , horizontalArrangement = Arrangement.SpaceBetween) {
            Column (modifier = Modifier.width(100.dp)) {
                Text(username.uppercase(), style = MaterialTheme.typography.labelSmall , color = MaterialTheme.colorScheme.background , maxLines = 1 , overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = { navController.popBackStack() } , modifier = Modifier
                .clip(
                    RoundedCornerShape(100.dp)
                )
                .size(26.dp)
                .background(MaterialTheme.colorScheme.background)
            ) {
                Image(imageVector = ImageVector.vectorResource(R.drawable.yim23_up_arrow), contentDescription = "Up arrow" , colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onBackground))
            }
            Column (modifier = Modifier.width(100.dp) , horizontalAlignment = Alignment.End) {
                Text("2023" , style = MaterialTheme.typography.labelSmall , color = MaterialTheme.colorScheme.background)
            }

        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 62.dp) , horizontalArrangement = Arrangement.Center) {
            Column (modifier = Modifier.width(250.dp)) {
                Text("MY YEAR IN MUSIC" , style = MaterialTheme.typography.titleLarge , color = MaterialTheme.colorScheme.surface , maxLines = 2 , textAlign = TextAlign.Center)
            }
        }
    }
}
