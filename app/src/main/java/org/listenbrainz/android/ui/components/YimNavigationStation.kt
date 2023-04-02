package org.listenbrainz.android.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.YimScreens
import org.listenbrainz.android.model.YimShareable
import org.listenbrainz.android.model.yimdata.TopRelease
import org.listenbrainz.android.viewmodel.YimViewModel


/** Shareable types : "stats", "artists", "albums", "tracks", "discovery-playlist", "missed-playlist".
 *
 * Pass empty array into [typeOfImage] to exclude share button.*/
@Composable
fun YimNavigationStation(
    typeOfImage: Array<YimShareable>,
    navController: NavController,
    viewModel: YimViewModel,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.padding(vertical = 50.dp),
    route: YimScreens
){
    val context = LocalContext.current
    val uriList : ArrayList<String> = arrayListOf()
    val topReleases : List<TopRelease>? = viewModel.getTopReleases()?.toList()
    topReleases?.forEach { item ->
        // https://archive.org/download/mbid-{caa_release_mbid}/mbid-{caa_release_mbid}-{caa_id}_thumb500.jpg
        if(uriList.size == 10) {
            return@forEach
        }
        uriList.add("https://archive.org/download/mbid-${item.caaReleaseMbid}/mbid-${item.caaReleaseMbid}-${item.caaId}_thumb500.jpg")
    }
    val imageUrlString: String = TextUtils.join(",", uriList)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if(route == YimScreens.YimChartsScreen){
            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://widgets.limurse.com/slider?imageUrls=$imageUrlString")
                    )
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                content = {
                    Text(
                        text = "Slider"
                    )
                }
            )
        }

        YimNavButton(goBack = true) {
            navController.popBackStack()
        }
        Spacer(modifier = Modifier.width(5.dp))
        if (typeOfImage.isNotEmpty()) {
            YimShareButton(
                viewModel = viewModel,
                typeOfImage = typeOfImage
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        YimNavButton {
            navController.navigate(route = route.name)
        }

        if(route == YimScreens.YimChartsScreen) {
            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://widgets.limurse.com/library?imageUrls=$imageUrlString")
                    )
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground, CircleShape),
                content = {
                    Text(
                        text = "Library"
                    )
                }
            )
        }
    }
}
