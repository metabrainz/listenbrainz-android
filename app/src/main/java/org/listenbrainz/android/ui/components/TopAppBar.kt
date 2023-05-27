package org.listenbrainz.android.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.dashboard.DonateActivity
import org.listenbrainz.android.ui.screens.settings.SettingsActivity

@Composable
fun TopBar(
    activity: Activity,
    navController: NavController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val title: String = currentDestination?.route?.let {
        when (it) {
            AppNavigationItem.Home.route -> AppNavigationItem.Home.title
            AppNavigationItem.BrainzPlayer.route -> AppNavigationItem.BrainzPlayer.title
            AppNavigationItem.Explore.route -> AppNavigationItem.Explore.title
            AppNavigationItem.Profile.route -> AppNavigationItem.Profile.title
            else -> "ListenBrainz"
        }
    } ?: "ListenBrainz"

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon =  {
            IconButton(onClick = {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://listenbrainz.org")))
            }) {
                Icon(painterResource(id = R.drawable.ic_listenbrainz_logo_icon),
                    "MusicBrainz",
                    tint = Color.Unspecified)
            }
        },
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                activity.startActivity(Intent(activity, org.listenbrainz.android.ui.screens.about.AboutActivity::class.java))
            }) {
                Icon(painterResource(id = R.drawable.ic_information),
                    "About",
                    tint = Color.Unspecified)
            }
            IconButton(onClick = {
                activity.startActivity(Intent(activity, DonateActivity::class.java))
            }) {
                Icon(painterResource(id = R.drawable.ic_donate), "Donate", tint = Color.Unspecified)
            }
            IconButton(onClick = {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }) {
                Icon(painterResource(id = R.drawable.action_settings),
                    "Settings",
                    tint = Color.Unspecified)
            }
        }
    )
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(activity = Activity(), navController = rememberNavController())
}