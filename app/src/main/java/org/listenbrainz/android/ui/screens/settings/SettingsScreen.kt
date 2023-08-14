package org.listenbrainz.android.ui.screens.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.isUiModeIsDark
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils.getActivity
import org.listenbrainz.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val darkTheme = onScreenUiModeIsDark()
    val darkThemeCheckedState = remember { mutableStateOf(darkTheme) }
    val submitListensCheckedState = remember { mutableStateOf(viewModel.appPreferences.submitListens) }
    val notificationsCheckedState = remember { mutableStateOf(viewModel.appPreferences.isNotificationServiceAllowed) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                notificationsCheckedState.value = viewModel.appPreferences.isNotificationServiceAllowed
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Send listens",
                color = MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = submitListensCheckedState.value,
                onCheckedChange = { submitListensCheckedState.value = it },
            )
        }

        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications enabled",
                color = MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = notificationsCheckedState.value,
                onCheckedChange = {
                    val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    } else {
                        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    }
                    context.startActivity(intent)
                },
            )
        }

        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dark theme",
                color = MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = darkThemeCheckedState.value,
                onCheckedChange = {
                    val intent = Intent(context, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
                    when (darkTheme) {
                        false -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            isUiModeIsDark.value = true
                            preferences.putString(
                                Constants.Strings.PREFERENCE_SYSTEM_THEME,
                                context.getString(R.string.settings_device_theme_dark)
                            ).apply()
                        }
                        true -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            isUiModeIsDark.value = false
                            preferences.putString(
                                Constants.Strings.PREFERENCE_SYSTEM_THEME,
                                context.getString(R.string.settings_device_theme_light)
                            ).apply()
                        }
                    }
                    context.getActivity()?.recreate() ?: context.startActivity(intent)
                    darkThemeCheckedState.value = it
                },
            )
        }

        Divider(thickness = 1.dp)

        // TODO: Decide whether we need a logout button or not
        //        Row(
        //            modifier = Modifier
        //                .fillMaxWidth()
        //                .padding(18.dp)
        //            ,
        //            verticalAlignment = Alignment.CenterVertically,
        //        ) {
        //            Text(
        //                text = "Logout",
        //                color = MaterialTheme.colorScheme.onSurface,
        //            )
        //        }
        //
        //        Divider(thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen()
    }
}