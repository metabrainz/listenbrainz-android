package org.listenbrainz.android.presentation.features.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerBackDropScreen
import org.listenbrainz.android.presentation.features.components.BottomNavigationBar
import org.listenbrainz.android.presentation.features.components.TopAppBar
import org.listenbrainz.android.presentation.features.onboarding.FeaturesActivity
import org.listenbrainz.android.presentation.theme.ListenBrainzTheme

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setUiMode()     // Set Ui Mode for XML layouts
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onboarding", false)) {
            startActivity(Intent(this, FeaturesActivity::class.java))
            finish()
        }
    
        val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        setContent {
            ListenBrainzTheme()
            {
                var isGrantedPerms by remember {
                    mutableStateOf(false)
                }
                val launcher = rememberLauncherForActivityResult(
                    contract =
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permission ->
                    val isGranted = permission.values.reduce{first,second->(first || second)}
                    if (!isGranted) {
                        Toast.makeText(this, "Brainzplayer requires local storage permissions to play local songs", Toast.LENGTH_SHORT).show()
                    } else {
                        isGrantedPerms = true
                    }
                }
                SideEffect {
                    launcher.launch(neededPermissions)
                }

                val backdropScaffoldState =
                    rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
                Scaffold(
                    topBar = { TopAppBar(activity = this, title = "Home") },
                    bottomBar = { BottomNavigationBar(activity = this) },
                    // This fixes the white flicker on start up that only occurs on BackLayerContent
                    backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) { paddingValues ->
                    if (isGrantedPerms) {
                        BrainzPlayerBackDropScreen(
                            backdropScaffoldState = backdropScaffoldState,
                            paddingValues = paddingValues,
                        ) {
                            BackLayerContent(activity = this)
                        }
                    }
                }
            }
        }
    }
    
    // Sets Ui mode for XML layouts.
    private fun setUiMode(){
        when(PreferenceManager.getDefaultSharedPreferences(this)
            .getString("app_theme", getString(R.string.settings_device_theme_use_device_theme)))
        {
            getString(R.string.settings_device_theme_dark) -> setDefaultNightMode(MODE_NIGHT_YES)
            getString(R.string.settings_device_theme_light) -> setDefaultNightMode(MODE_NIGHT_NO)
            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
