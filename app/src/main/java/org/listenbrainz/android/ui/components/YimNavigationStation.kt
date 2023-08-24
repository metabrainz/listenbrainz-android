package org.listenbrainz.android.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.model.yimdata.YimShareable
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
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
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
    }
}
