package org.listenbrainz.android.ui.navigation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

data class TopBarActions(
    val popBackStackInSettingsScreen: ()-> Unit = {},
    val navigateToSettingsScreen: ()-> Unit = {},
    val activateSearch: () -> Unit = {}
)

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    backgroundColor: Color = Color.Transparent,
    context: Context = LocalContext.current,
    topBarActions: TopBarActions
) {
        TopAppBar(
            modifier = modifier,
            title = { Text(text = title) },
            navigationIcon = {
                IconButton(onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://listenbrainz.org")
                        )
                    )
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_listenbrainz_logo_icon),
                        "ListenBrainz",
                        tint = Color.Unspecified
                    )
                }
            },
            backgroundColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = 0.dp,
            actions = {
                IconButton(onClick = { topBarActions.activateSearch() }) {
                    Icon(
                        painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search users"
                    )
                }

                IconButton(onClick = {
                    if (title == AppNavigationItem.Settings.title) {
                        topBarActions.popBackStackInSettingsScreen()
                    } else {
                        topBarActions.navigateToSettingsScreen()
                    }
                }) {
                    Icon(painterResource(id = R.drawable.ic_settings), "Settings")
                }
            }
        )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TopBarPreview() {
    ListenBrainzTheme {
        TopBar(
            topBarActions = TopBarActions(
                popBackStackInSettingsScreen = {},
                navigateToSettingsScreen = {},
                activateSearch = {}
            ),
            title = "ListenBrainz",
        )
    }
}