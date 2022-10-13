package org.listenbrainz.android.presentation.features.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.presentation.components.BottomNavigationBar
import org.listenbrainz.android.presentation.components.TopAppBar
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerBackDropScreen
import org.listenbrainz.android.presentation.features.onboarding.FeaturesActivity

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onboarding", false)) {
            startActivity(Intent(this, FeaturesActivity::class.java))
            finish()
        }
        val neededPermissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
        setContent {
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
                bottomBar = { BottomNavigationBar(activity = this) }
            ) { paddingValues ->
                if (isGrantedPerms) {
                    BrainzPlayerBackDropScreen(
                        backdropScaffoldState = backdropScaffoldState,
                        paddingValues = paddingValues,
                    ) {
                        BackLayerContent(activity = this, applicationContext = LocalContext.current)
                    }
                }
            }
        }
    }
}
