@file:OptIn(ExperimentalMaterial3Api::class)

package org.listenbrainz.android.ui.screens.onboarding.listeningApps

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night


@Composable
fun AllInstalledAppsBottomSheet(
    appsList: List<AppInfo>,
    onListeningAppsAdded: (List<AppInfo>) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedApps by remember { mutableStateOf(appsList.filter { it.isListening }) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredApps = remember(appsList, searchQuery) {
        if (searchQuery.isBlank()) {
            appsList
        } else {
            appsList.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedAppsListState = rememberLazyListState()
    val allAppsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedApps.size) {
        if (selectedApps.isNotEmpty()) {
            coroutineScope.launch {
                selectedAppsListState.animateScrollToItem(selectedApps.size - 1)
            }
        }
    }

    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            if (appsList.isNotEmpty()) {
                allAppsListState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ListenBrainzTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        HeaderSection(
            isSearching = isSearching,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearchToggle = {
                isSearching = !isSearching
                if (!isSearching) {
                    searchQuery = ""
                    keyboardController?.hide()
                }
            },
            focusRequester = focusRequester
        )

        Spacer(modifier = Modifier.height(16.dp))

        SelectedAppsSection(
            selectedApps = selectedApps,
            onRemoveApp = { app ->
                selectedApps = selectedApps.filter { it.packageName != app.packageName }
            },
            listState = selectedAppsListState
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.12f))

        Spacer(modifier = Modifier.height(16.dp))

        AllAppsSection(
            apps = filteredApps,
            selectedApps = selectedApps,
            onAppToggle = { app, isSelected ->
                selectedApps = if (isSelected) {
                    selectedApps + app
                } else {
                    selectedApps.filter { it.packageName != app.packageName }
                }
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        BottomButtonsSection(
            onCancel = onCancel,
            onDone = {
                onListeningAppsAdded(selectedApps)
                onDone()
            }
        )
    }
}

@Composable
private fun HeaderSection(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearching) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ListenBrainzTheme.colorScheme.text
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = ListenBrainzTheme.colorScheme.text,
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(ListenBrainzTheme.colorScheme.text),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search apps...",
                                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f),
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        } else {
            Text(
                text = "All installed apps",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = ListenBrainzTheme.colorScheme.listenText
            )

            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ListenBrainzTheme.colorScheme.listenText
                )
            }
        }
    }
}

@Composable
private fun SelectedAppsSection(
    selectedApps: List<AppInfo>,
    onRemoveApp: (AppInfo) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Choose the apps you want ListenBrainz to track. This helps automatically submit your music listens.",
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${selectedApps.size} Apps Selected",
            fontWeight = FontWeight.Medium,
            color = ListenBrainzTheme.colorScheme.text,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))
        if (selectedApps.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ListenBrainzTheme.shapes.listenCardSmall)
                    .background(color = Color.Gray.copy(0.25f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(selectedApps) { app ->
                    SelectedAppIcon(
                        app = app,
                        onRemove = { onRemoveApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedAppIcon(
    app: AppInfo,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clickable { onRemove() }
    ) {
        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.appName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))

        )

        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color.Red, CircleShape)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun AllAppsSection(
    apps: List<AppInfo>,
    selectedApps: List<AppInfo>,
    onAppToggle: (AppInfo, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "All apps",
            fontWeight = FontWeight.Medium,
            color = ListenBrainzTheme.colorScheme.text,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .clip(ListenBrainzTheme.shapes.listenCardSmall)
                .background(color = Color.Gray.copy(0.25f))
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            itemsIndexed(apps) { ind, app ->
                val isSelected = selectedApps.any { it.packageName == app.packageName }
                AppListItem(
                    app = app,
                    isSelected = isSelected,
                    onToggle = { onAppToggle(app, !isSelected) },
                    isLast = ind == apps.lastIndex
                )
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val selectedColor = if (isSystemInDarkTheme()) lb_purple_night else lb_purple
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onToggle,
            colors = RadioButtonDefaults.colors(
                selectedColor = if (isSelected) selectedColor else ListenBrainzTheme.colorScheme.text.copy(
                    alpha = 0.6f
                ),
                unselectedColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.appName,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = app.appName,
                color = ListenBrainzTheme.colorScheme.text,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(16.dp))
            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxSize(),
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun BottomButtonsSection(
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSystemInDarkTheme()) lb_purple_night else lb_purple
            )
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(
            onClick = onDone,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSystemInDarkTheme()) lb_purple_night else lb_purple
            )
        ) {
            Text(
                text = "Done",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AllInstalledAppsBottomSheetPreview() {
    ListenBrainzTheme {
        AllInstalledAppsBottomSheet(
            appsList = getSampleApps(),
            onListeningAppsAdded = { },
            onCancel = { },
            onDone = { }
        )
    }
}

private fun getSampleApps(): List<AppInfo> {
    return listOf(
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "YouTube",
            "com.google.android.youtube",
            createSampleBitmap(),
            false,
            false
        ),
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "Spotify",
            "com.spotify.music",
            createSampleBitmap(),
            false,
            true
        ),
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "YouTube Music",
            "com.google.android.apps.youtube.music",
            createSampleBitmap(),
            false,
            false
        ),
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "Amazon Music",
            "com.amazon.mp3",
            createSampleBitmap(),
            false,
            true
        ),
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "ListenBrainz",
            "org.metabrainz.android",
            createSampleBitmap(),
            false,
            true
        ),
        _root_ide_package_.org.listenbrainz.android.ui.screens.onboarding.listeningApps.AppInfo(
            "Audiomack",
            "com.audiomack",
            createSampleBitmap(),
            false,
            false
        )
    )
}

private fun createSampleBitmap(): Bitmap {
    return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
        eraseColor(android.graphics.Color.BLUE)
    }
}