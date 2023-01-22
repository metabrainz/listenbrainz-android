package org.listenbrainz.android.presentation.features.dashboard

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.*
import androidx.core.content.PermissionChecker
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.UserPreferences.PermissionStatus
import org.listenbrainz.android.presentation.UserPreferences.permissionsPreference
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerBackDropScreen
import org.listenbrainz.android.presentation.features.components.BottomNavigationBar
import org.listenbrainz.android.presentation.features.components.TopAppBar
import org.listenbrainz.android.presentation.features.onboarding.FeaturesActivity
import org.listenbrainz.android.presentation.theme.ListenBrainzTheme
import org.listenbrainz.android.util.uicomponents.DialogLB

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
        
        // TODO: Rework permissions
        val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                permission.READ_MEDIA_IMAGES,
                permission.READ_MEDIA_AUDIO
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            arrayOf(permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE)
        }
        
        updatePermissionPreference()
    
        setContent {
            ListenBrainzTheme()
            {
                var isGrantedPerms by remember {
                    mutableStateOf(permissionsPreference)
                }
                
                val launcher = rememberLauncherForActivityResult(
                    contract =
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permission ->
                    val isGranted = permission.values.reduce{first,second->(first || second)}
                    if (isGranted) {
                        isGrantedPerms = PermissionStatus.GRANTED.name
                        permissionsPreference  = PermissionStatus.GRANTED.name
                    }else{
                        isGrantedPerms = when(isGrantedPerms){
                            PermissionStatus.NOT_REQUESTED.name -> {
                                PermissionStatus.DENIED_ONCE.name
                            }
                            PermissionStatus.DENIED_ONCE.name -> {
                                PermissionStatus.DENIED_TWICE.name
                            }
                            else -> {PermissionStatus.DENIED_TWICE.name}
                        }
                        permissionsPreference = isGrantedPerms
                    }
                }
                
                LaunchedEffect(Unit) {
                    if (isGrantedPerms == PermissionStatus.NOT_REQUESTED.name) {
                        launcher.launch(neededPermissions)
                    }
                }
                
                when(isGrantedPerms){
                    PermissionStatus.DENIED_ONCE.name -> {
                        DialogLB(
                            options = arrayOf("Grant"),
                            firstOptionListener = {
                                launcher.launch(neededPermissions)
                            },
                            title = "Permissions required",
                            description = "BrainzPlayer requires local storage permission to play local songs.",
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            onDismiss = {}
                        )
                    }
                    PermissionStatus.DENIED_TWICE.name -> {
                        DialogLB(
                            title = "Permissions required",
                            description = "Please grant storage permissions from settings for the app to function.",
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            onDismiss = {}
                        )
                    }
                }

                val backdropScaffoldState =
                    rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
                Scaffold(
                    topBar = { TopAppBar(activity = this, title = "Home") },
                    bottomBar = { BottomNavigationBar(activity = this) },
                    backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                ) { paddingValues ->
                    if (isGrantedPerms == PermissionStatus.GRANTED.name) {
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
    
    // If the user enables permission from settings, this function updates the preference.
    private fun updatePermissionPreference(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                checkSelfPermission(permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            ){
                permissionsPreference = PermissionStatus.GRANTED.name
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (checkSelfPermission(permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                permissionsPreference = PermissionStatus.GRANTED.name
            }
        } else {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                } else {
                    PermissionChecker.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED &&
                    PermissionChecker.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED
                }
            ){
                permissionsPreference = PermissionStatus.GRANTED.name
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
