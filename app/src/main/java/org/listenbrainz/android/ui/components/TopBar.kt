package org.listenbrainz.android.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.screens.dashboard.DonateActivity
import org.listenbrainz.android.ui.theme.isUiModeIsDark
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.util.Constants

@Composable
fun TopBar(
    activity: Activity,
    navController: NavController = rememberNavController(),
    context: Context = LocalContext.current
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

    val darkTheme = onScreenUiModeIsDark()
    val themeIcon = remember(darkTheme) {
        mutableStateOf(
            when (darkTheme) {
                true -> {
                    R.drawable.moon_regular
                }
                else -> {
                    R.drawable.moon_solid
                }
            }
        )
    }

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
            // TODO: Move about-section to settings.
            /*IconButton(onClick = {
                activity.startActivity(Intent(activity, org.listenbrainz.android.ui.screens.about.AboutActivity::class.java))
            }) {
                Icon(painterResource(id = R.drawable.ic_information),
                    "About",
                    tint = Color.Unspecified)
            }*/
            IconButton(onClick = {
                navController.navigate(route = AppNavigationItem.Search.route){
                    // Avoid building large backstack
                    popUpTo(AppNavigationItem.Home.route)
                    // Avoid copies
                    launchSingleTop = true
            
                }
            }) {
                Icon(painterResource(id = AppNavigationItem.Search.iconSelected),
                    AppNavigationItem.Search.title,
                    tint = Color.Unspecified)
            }
            IconButton(onClick = {
                activity.startActivity(Intent(activity, DonateActivity::class.java))
            }) {
                Icon(painterResource(id = R.drawable.ic_donate), "Donate", tint = Color.Unspecified)
            }
            IconButton(onClick = {
                val intent = Intent(activity, DashboardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
                when (themeIcon.value) {
                    R.drawable.moon_solid -> {
                        setDefaultNightMode(MODE_NIGHT_YES)
                        isUiModeIsDark.value = true
                        preferences.putString(
                            Constants.Strings.PREFERENCE_SYSTEM_THEME,
                            context.getString(R.string.settings_device_theme_dark)
                        ).apply()
                    }
                    R.drawable.moon_regular -> {
                        setDefaultNightMode(MODE_NIGHT_NO)
                        isUiModeIsDark.value = false
                        preferences.putString(
                            Constants.Strings.PREFERENCE_SYSTEM_THEME,
                            context.getString(R.string.settings_device_theme_light)
                        ).apply()
                    }
                    else -> {
                        setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                        isUiModeIsDark.value = null
                        preferences.putString(
                            Constants.Strings.PREFERENCE_SYSTEM_THEME,
                            context.getString(R.string.settings_device_theme_use_device_theme)
                        ).apply()
                    }
                }
                activity.startActivity(intent)
            }) {
                Icon(painterResource(id = themeIcon.value),
                    "Theme",
                    tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar(activity = Activity(), navController = rememberNavController())
}