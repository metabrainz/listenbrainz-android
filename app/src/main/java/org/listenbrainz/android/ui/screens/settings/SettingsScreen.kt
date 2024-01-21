package org.listenbrainz.android.ui.screens.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.limurse.logger.Logger
import com.limurse.logger.util.FileIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.R
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.screens.listens.ListeningAppsList
import org.listenbrainz.android.ui.screens.main.DonateActivity
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    listensViewModel: ListensViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showBlacklist by remember { mutableStateOf(false) }
    val darkTheme = ListenBrainzTheme.uiModeIsDark
    val scope = rememberCoroutineScope()
    
    val darkThemeCheckedState = remember { mutableStateOf(darkTheme) }
    /** This preference state can only be changed when the user exits the app, and we always update
     * this state when the user exits the app, this will always be true.*/
    var isNotificationServiceAllowed by remember {
        mutableStateOf(viewModel.appPreferences.isNotificationServiceAllowed)
    }
    val submitListensCheckedState by viewModel.appPreferences.isListeningAllowed
        .getFlow().collectAsState(initial = true)
    val shouldScrobbleNewPlayers by viewModel.appPreferences
        .shouldListenNewPlayers.getFlow().collectAsState(initial = true)
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                isNotificationServiceAllowed = withContext(Dispatchers.IO) {
                    viewModel.appPreferences.isNotificationServiceAllowed
                }
            }
            else -> Unit
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp)
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
            Column {
                Text(
                    text = "Send listens",
                    color = when {
                        isNotificationServiceAllowed -> MaterialTheme.colorScheme.onSurface
                        else -> Color(0xFF949494)
                    }
                )

                Text(
                    text = "Enable sending listens from this device to ListenBrainz",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(0.9f)
                )
            }

            Switch(
                checked = submitListensCheckedState && isNotificationServiceAllowed,
                onCheckedChange = { checked ->
                    scope.launch {
                        if (isNotificationServiceAllowed) {
                            // Set preference
                            viewModel.appPreferences.isListeningAllowed.set(checked)
                        }
                    }
                    
                }
            )
        }

        Divider(thickness = 1.dp)

        // Blacklist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clickable {
                    if (isNotificationServiceAllowed) {
                        showBlacklist = true
                    }
                }
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Listening apps",
                    color = when {
                        isNotificationServiceAllowed -> MaterialTheme.colorScheme.onSurface
                        else -> Color(0xFF949494)
                    }
                )

                Text(
                    text = "Enable sending listens from individual apps on this device",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(240.dp)
                )
            }
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
            Column {
                Text(
                    text = "Notifications",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Required to send listens",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(0.9f)
                )
            }

            Switch(
                checked = isNotificationServiceAllowed,
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
            Column {
                Text(
                    text = "Enable new players",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "When a new music app is detected, automatically use it to submit listens",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp, end = 6.dp)
                        .fillMaxWidth(0.9f)
                )
            }

            Switch(
                checked = shouldScrobbleNewPlayers,
                onCheckedChange = {
                    scope.launch {
                        viewModel.appPreferences.shouldListenNewPlayers.set(it)
                    }
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
            Column {
                Text(
                    text = "Dark theme",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Enable the dark theme on this device",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth(0.9f)
                )
            }

            Switch(
                checked = darkThemeCheckedState.value,
                onCheckedChange = {
                    when (darkTheme) {
                        false -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            scope.launch {
                                viewModel.appPreferences.themePreference.set(UiMode.DARK)
                            }
                        }
                        true -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            scope.launch {
                                viewModel.appPreferences.themePreference.set(UiMode.LIGHT)
                            }
                        }
                    }
                    darkThemeCheckedState.value = it
                },
            )
        }

        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clickable {
                    Logger.apply {
                        compressLogsInZipFile { zipFile ->
                            zipFile?.let {
                                FileIntent.fromFile(
                                    context,
                                    it,
                                    BuildConfig.APPLICATION_ID
                                )?.let { intent ->
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Log Files")
                                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("mobile@metabrainz.org"))
                                    intent.putExtra(Intent.EXTRA_TEXT, "Please find the attached log files.")
                                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", zipFile))
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    try {
                                        context.startActivity(Intent.createChooser(intent, "Email logs..."))
                                    } catch (e: java.lang.Exception) {
                                        e(throwable = e)
                                    }
                                }
                            }
                        }
                    }
                }
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Report an issue",
                    color = when {
                        isNotificationServiceAllowed -> MaterialTheme.colorScheme.onSurface
                        else -> Color(0xFF949494)
                    }
                )

                Text(
                    text = "Submit app logs for further investigation",
                    lineHeight = 18.sp,
                    fontSize = 12.sp,
                    color = Color(0xFF949494),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(240.dp)
                )
            }
        }

        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "About",
                color = Color(0xFF908EAF)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Constants.ABOUT_URL.toUri()
                    context.startActivity(intent)
                }
            ,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "About ListenBrainz",
                color = MaterialTheme.colorScheme.onSurface
            )

            Image(
                painter = painterResource(id = R.drawable.link_to),
                contentDescription = "Arrow",
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
                .clickable {
                    context.startActivity(Intent(context, DonateActivity::class.java))
                }
            ,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Support MetaBrainz",
                color = MaterialTheme.colorScheme.onSurface
            )

            Image(
                painter = painterResource(id = R.drawable.link_to),
                contentDescription = "Arrow",
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "v. ${viewModel.version()}",
                color = Color(0xFF949494)
            )
        }

        Divider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 18.dp, end = 18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attributions",
                color = Color(0xFF908EAF)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val annotatedStringAttributions: AnnotatedString = buildAnnotatedString {
                val originalString =
                        "Animations by Korhan Ulusoy, Jake Cowan, KidA Studio, puput Santoso , Charts by Patrick Michalik and Paul Roux on LottieFiles from lottiefiles.com\n\n" +
                        "The complete resources with links can be found at\n" +
                        "https://github.com/metabrainz/listenbrainz-android/blob/main/asset_attributions.md"
                val startIndexGithub = originalString.indexOf("https://github.com")
                val endIndexGithub = startIndexGithub + 82
                append(originalString)
                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline
                    ), start = startIndexGithub, end = endIndexGithub
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://github.com/metabrainz/listenbrainz-android/blob/main/asset_attributions.md",
                    start = startIndexGithub,
                    end = endIndexGithub
                )
            }

            ClickableText(
                text = annotatedStringAttributions,

                style = TextStyle(
                    color = Color(0xFF949494),
                    fontSize = 12.sp
                ),
                onClick = { offset ->
                    annotatedStringAttributions
                        .getStringAnnotations("URL", offset, offset)
                        .firstOrNull()?.let { stringAnnotation ->
                            uriHandler.openUri(stringAnnotation.item)
                        }
                }
            )
        }

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

        // BlackList Dialog
        val uiState by listensViewModel.preferencesUiState.collectAsState()
        if (showBlacklist) {
            ListeningAppsList(
                preferencesUiState = uiState,
                fetchLinkedServices = {
                     listensViewModel.fetchLinkedServices()
                },
                getPackageIcon = { packageName ->
                    listensViewModel.getPackageIcon(packageName)
                },
                getPackageLabel = { packageName ->
                    listensViewModel.getPackageLabel(packageName)
                },
                setWhitelist = { newList ->
                    listensViewModel.setWhitelist(newList)
                },
            ) { showBlacklist = false }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    ListenBrainzTheme {
        SettingsScreen(
             viewModel = hiltViewModel(),
            listensViewModel = hiltViewModel(),
        )
    }
}