package org.listenbrainz.android.ui.screens.playlist

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.feed.RetryButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPlaylistScreen(
    viewModel: PlaylistDataViewModel = koinViewModel(),
    isVisible: Boolean,
    mbid: String?,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    LaunchedEffect(uiState.createEditScreenUIState.isSearching) {
        sheetState.expand()
    }

    if (isVisible) {
        LaunchedEffect(Unit) {
            viewModel.getInitialDataInCreatePlaylistScreen(mbid)
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ModalBottomSheet(
                modifier = Modifier.statusBarsPadding(),
                onDismissRequest = dismissWithAnimation,
                sheetState = sheetState
            ) {
                AnimatedContent(uiState.createEditScreenUIState.isLoading) { isLoading ->
                    if (!isLoading) {
                        if (uiState.createEditScreenUIState.playlistData != null || uiState.createEditScreenUIState.playlistMBID == null)
                            CreateEditPlaylistScreenBase(
                                collaborators = uiState.createEditScreenUIState.collaboratorSelected,
                                onCollaboratorAdded = {
                                    viewModel.editPlaylistScreenData(
                                        collaborators = uiState.createEditScreenUIState.collaboratorSelected + it
                                    )
                                },
                                onSave = {
                                    viewModel.saveNewOrEditedPlaylist {
                                        scope.launch {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        }
                                        dismissWithAnimation()
                                    }
                                },
                                uiState = uiState.createEditScreenUIState,
                                onCollaboratorRemove = { user ->
                                    viewModel.editPlaylistScreenData(
                                        collaborators = uiState.createEditScreenUIState.collaboratorSelected.filter { it != user }
                                    )
                                },
                                onNameChange = {
                                    viewModel.editPlaylistScreenData(name = it)
                                },
                                onDescriptionChange = {
                                    viewModel.editPlaylistScreenData(description = it)
                                },
                                onVisibilityChange = {
                                    viewModel.editPlaylistScreenData(isPublic = it)
                                },
                                onCancel = {
                                    dismissWithAnimation()
                                },
                                onUsernameQueryChange = {
                                    viewModel.queryCollaborators(it)
                                }
                            )
                        else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                RetryButton(
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    viewModel.getInitialDataInCreatePlaylistScreen(mbid)
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation()
                        }
                    }
                }
            }
            ErrorBar(uiState.error) {
                viewModel.clearErrorFlow()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateEditPlaylistScreenBase(
    uiState: CreateEditScreenUIState,
    collaborators: List<String>,
    onCollaboratorAdded: (String) -> Unit,
    onCollaboratorRemove: (String) -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    onUsernameQueryChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    val localFocusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(uiState.isSearching) {
        lazyListState.animateScrollToItem(
            //Here 4 is the index of the Collaborator Input Field.
            4
        )
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Name Field
            Text(
                "Name",
                style = ListenBrainzTheme.textStyles.listenTitle,
                color = ListenBrainzTheme.colorScheme.text,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            PlaylistTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                placeholderText = "Enter playlist name",
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(), isError = uiState.emptyTitleFieldError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        localFocusManager.moveFocus(FocusDirection.Down)
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {

            // Description Field
            Text(
                "Description",
                style = ListenBrainzTheme.textStyles.listenTitle,
                color = ListenBrainzTheme.colorScheme.text,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            PlaylistTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholderText = "Enter description",
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Make Playlist Public Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isPublic,
                    onCheckedChange = onVisibilityChange,
                    colors = CheckboxDefaults.colors().copy(
                        checkedBoxColor = lb_purple_night,
                        checkedBorderColor = ListenBrainzTheme.colorScheme.listenText
                    )
                )
                Text(
                    "Make playlist public",
                    style = ListenBrainzTheme.textStyles.listenTitle,
                    color = ListenBrainzTheme.colorScheme.text,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Collaborators Section
            Text(
                "Collaborators",
                color = ListenBrainzTheme.colorScheme.text,
                style = ListenBrainzTheme.textStyles.listenTitle,
                fontSize = 16.sp
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                collaborators.forEach { user ->
                    CollaboratorAssistChip(
                        userName = user,
                        onRemove = {
                            onCollaboratorRemove(user)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            // Collaborator Input Field with Suggestions
            CollaboratorInputField(
                allUsers = uiState.usersSearched.map { it.username },
                username = uiState.collaboratorQueryText,
                onCollaboratorAdded = { onCollaboratorAdded(it) },
                onUsernameQueryChange = { onUsernameQueryChange(it) },
                isSearching = uiState.isSearching,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        localFocusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }


        item {
            // Buttons Row
            Row(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    enabled = !uiState.isSaving,
                    onClick = { onCancel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(
                    Modifier
                        .width(8.dp)
                        .weight(0.1f)
                )
                Button(
                    onClick = { onSave() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) lb_orange else lb_purple,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (uiState.playlistMBID == null)
                            "Create"
                        else "Save"
                    )
                }
            }
        }
    }
}

@Composable
fun CollaboratorAssistChip(
    modifier: Modifier = Modifier,
    userName: String,
    onRemove: () -> Unit
) {
    AssistChip(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        onClick = { onRemove() },
        border = null,
        colors =
            AssistChipDefaults.assistChipColors().copy(
                containerColor = lb_purple_night,
                labelColor = Color.White
            ),
        label = { Text(userName) },
        trailingIcon = {
            Box(
                modifier = Modifier.size(20.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White,
                    )
                }
                Icon(
                    Icons.Default.Close, contentDescription = "Remove",
                    tint = lb_purple_night,
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .align(Alignment.Center)
                )
            }
        }
    )

}


@Composable
fun CollaboratorInputField(
    username: String,
    allUsers: List<String>,
    onCollaboratorAdded: (String) -> Unit,
    onUsernameQueryChange: (String) -> Unit,
    isSearching: Boolean,
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
    ) {
        PlaylistTextField(
            value = username,
            onValueChange = {
                onUsernameQueryChange(it)
                expanded = it.isNotEmpty()
            },
            placeholderText = "Enter Collaborators",
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.hasFocus)
                        expanded = false
                },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (expanded) {
            if (!isSearching)
                allUsers.filterIndexed { index, s -> index < 20 }.forEach() { username ->
                    SearchItem(text = username) {
                        onCollaboratorAdded(username)
                        expanded = false
                    }
                } else {
                Box(Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(
                        color = ListenBrainzTheme.colorScheme.listenText,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(8.dp)
                    )
                }
            }
            if (!isSearching && allUsers.isEmpty()) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        "No users found",
                        modifier = Modifier.align(Alignment.Center),
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
                    )
                }
            }
        }

    }
}

@Composable
private fun PlaylistTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedIndicatorColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f),
            cursorColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f)
        ),
        placeholder = {
            Text(placeholderText)
        },
        isError = isError,
        supportingText = {
            if (isError)
                Text("Playlist title must not be empty.")
        },
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions
    )
}

@Composable
private fun SearchItem(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = text,
            color = ListenBrainzTheme.colorScheme.text
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}


@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CreateEditPlaylistScreenPreview() {
    ListenBrainzTheme {
        val allUsers = listOf("raven0us", "jasje", "hemang", "musiclover") // Example usernames
        CreateEditPlaylistScreenBase(
            collaborators = allUsers,
            onCollaboratorAdded = {},
            onSave = {},
            uiState = CreateEditScreenUIState(),
            onCollaboratorRemove = { },
            onNameChange = { },
            onDescriptionChange = { },
            onVisibilityChange = { },
            onCancel = { },
            onUsernameQueryChange = {},
        )
    }
}