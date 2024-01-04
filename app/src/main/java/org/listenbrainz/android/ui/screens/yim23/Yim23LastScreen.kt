package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23TopDiscoveries
import org.listenbrainz.android.model.yimdata.Yim23Track
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23LastScreen (
    viewModel: Yim23ViewModel,
    navController: NavController
) {
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), verticalArrangement = Arrangement.SpaceBetween) {
            Box (modifier = Modifier.padding(top = 10.dp)) {
                Column {
                    Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center , verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                                         navController.navigate(route = Yim23Screens.YimLandingScreen.name)
                        } , colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onBackground)) {
                            Text("START AGAIN" , style = MaterialTheme.typography.bodySmall , color = MaterialTheme.colorScheme.background)
                        }
                        Spacer(modifier = Modifier.padding(start = 10.dp , end = 10.dp))
                        IconButton(onClick = { navController.popBackStack() } , modifier = Modifier
                            .clip(
                                RoundedCornerShape(100.dp)
                            )
                            .size(26.dp)
                            .background(MaterialTheme.colorScheme.onBackground)
                        ) {
                            Image(imageVector = ImageVector.vectorResource(R.drawable.yim23_up_arrow), contentDescription = "Up arrow" , colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.background))
                        }
                    }
                    Spacer(modifier = Modifier.padding(top = 5.dp , bottom = 5.dp))
                    Row (modifier = Modifier.fillMaxWidth() , horizontalArrangement = Arrangement.Center) {
                        Text("#YEAR IN MUSIC" , color = MaterialTheme.colorScheme.onBackground , style = MaterialTheme.typography.titleLarge)
                    }
                }

            }
            
            Box () {
                Column (modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 11.dp, end = 11.dp) , horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.yim23_g_heart), contentDescription = "Heart illustration" , modifier = Modifier
                        .height(123.dp)
                        .width(108.dp))
                    Text("Wishing you a restful 2024,\u2028from the ListenBrainz team." , color = MaterialTheme.colorScheme.onBackground , style = MaterialTheme.typography.bodyMedium , textAlign = TextAlign.Center)
                    Text("If you have questions or feedback don't hesitate to contact us on our forums, by email, IRC, X, Bluesky or Mastodon\n" , color = MaterialTheme.colorScheme.onBackground , style = MaterialTheme.typography.bodyMedium , textAlign = TextAlign.Center)
                }
            }

            Box (modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.onBackground)) {
                Column {
                    Column (verticalArrangement = Arrangement.SpaceEvenly , horizontalAlignment = Alignment.CenterHorizontally , ) {
                        androidx.compose.material.Text(
                            username.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.paddingFromBaseline(top = 40.dp)
                        )
                        Row (modifier = Modifier.fillMaxWidth() , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Center) {
                            Yim23ShareButton()
                            ListenBrainzProfileButton()
                            AddUser()
                        }
                    }
                }
            }
        }
    }
}


