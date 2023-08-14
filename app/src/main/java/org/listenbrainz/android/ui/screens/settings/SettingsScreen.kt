package org.listenbrainz.android.ui.screens.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.preference.PreferenceManager
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.about.AboutScreen
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.screens.onboarding.FeaturesActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.isUiModeIsDark
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.util.Utils.getActivity
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
        .verticalScroll(rememberScrollState())
    ) {
        androidx.compose.material.IconButton(onClick = {
            val intent = Intent(context, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
            when (themeIcon.value) {
                R.drawable.moon_solid -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    isUiModeIsDark.value = true
                    preferences.putString(
                        Constants.Strings.PREFERENCE_SYSTEM_THEME,
                        context.getString(R.string.settings_device_theme_dark)
                    ).apply()
                }

                R.drawable.moon_regular -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    isUiModeIsDark.value = false
                    preferences.putString(
                        Constants.Strings.PREFERENCE_SYSTEM_THEME,
                        context.getString(R.string.settings_device_theme_light)
                    ).apply()
                }

                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    isUiModeIsDark.value = null
                    preferences.putString(
                        Constants.Strings.PREFERENCE_SYSTEM_THEME,
                        context.getString(R.string.settings_device_theme_use_device_theme)
                    ).apply()
                }
            }
            context.getActivity()?.recreate() ?: context.startActivity(intent)
        }) {
            androidx.compose.material.Icon(
                painterResource(id = themeIcon.value),
                "Theme change",
                tint = MaterialTheme.colorScheme.onSurface
            )

        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked = false,
                onCheckedChange = {  },
            )
        }

        Button(modifier = Modifier.padding(start = 16.dp), onClick = { viewModel.logout() }) {
            androidx.compose.material.Text(
                text = "Logout",
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.material.MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen()
    }
}