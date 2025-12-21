package org.listenbrainz.android.ui.screens.feed

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.PersonalRecommendationDialog
import org.listenbrainz.android.ui.components.dialogs.PinDialog
import org.listenbrainz.android.ui.components.dialogs.ReviewDialog
import org.listenbrainz.android.ui.components.dialogs.SocialDropdownSheets
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.playlist.CreateEditPlaylistScreen
import org.listenbrainz.android.ui.screens.playlist.SelectPlaylist
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getActivity
import org.listenbrainz.android.viewmodel.SocialViewModel

/** This layer tries to define what options in the dropdown menu are to be shown.*/
@Composable
fun SocialDropdownDefault(
    isExpanded: Boolean,
    metadata: Metadata?,
    onError: suspend CoroutineScope.(ResponseError) -> Unit,
    onSuccess: suspend CoroutineScope.(message: String) -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onDropdownDismiss: () -> Unit,
) {
    if (metadata == null || LocalView.current.isInEditMode) return

    val context = LocalContext.current
    val viewModel: SocialViewModel = hiltViewModel(LocalContext.current.getActivity()!!)
    val uriHandler = LocalUriHandler.current
    val dialogsState = rememberDialogsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentSheet by remember { mutableStateOf(SocialDropdownSheets.NONE) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            onError(error)
            viewModel.clearErrorFlow()
        }
    }

    LaunchedEffect(key1 = uiState.successMsgId) {
        uiState.successMsgId?.let {
            onSuccess(context.getString(it))
            viewModel.clearMsgFlow()
        }
    }

    SocialDropdown(
        isExpanded = isExpanded,
        onDismiss = onDropdownDismiss,
        metadata = metadata,
        onOpenInMusicBrainz = onOpenInMusicBrainz@{
            uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid ?: return@onOpenInMusicBrainz}")
        },
        onPin = {
            dialogsState.activateDialog(Dialog.PIN)
        },
        onRecommend = {
            viewModel.recommend(metadata)
            onDropdownDismiss()
        },
        onPersonallyRecommend = {
            dialogsState.activateDialog(Dialog.PERSONAL_RECOMMENDATION)
        },
        onReview = {
            dialogsState.activateDialog(Dialog.REVIEW)
        },
        onAddToPlaylist = {
            currentSheet = SocialDropdownSheets.SELECT_PLAYLIST_TO_ADD_TRACK
            onDropdownDismiss()
        },
        onRemoveFromPlaylist = if(onRemoveFromPlaylist != null) {
            {
                onRemoveFromPlaylist()
                onDropdownDismiss()
            }
        } else null,
    )

    LaunchedEffect(key1 = dialogsState.currentDialog) {
        if (dialogsState.currentDialog != Dialog.NONE) {
            onDropdownDismiss()
        }
    }

    when (dialogsState.currentDialog) {
        Dialog.NONE -> Unit
        Dialog.PIN -> {
            PinDialog(
                trackName = metadata.trackMetadata?.trackName
                    ?: metadata.entityName ?: return,
                artistName = metadata.trackMetadata?.artistName ?: return,
                onDismiss = dialogsState::deactivateDialog,
                onSubmit = { blurbContent ->
                    viewModel.pin(metadata, blurbContent)
                }
            )
        }

        Dialog.PERSONAL_RECOMMENDATION -> {
            PersonalRecommendationDialog(
                trackName = metadata.trackMetadata?.trackName
                    ?: metadata.entityName ?: return,
                onDismiss = dialogsState::deactivateDialog,
                searchResult = uiState.searchResult,
                searchUsers = viewModel::searchUser,
                onSubmit = { users, blurbContent ->
                    viewModel.personallyRecommend(metadata, users, blurbContent)
                }
            )
        }

        Dialog.REVIEW -> {
            ReviewDialog(
                trackName = metadata.trackMetadata?.trackName
                    ?: if (metadata.entityType == ReviewEntityType.RECORDING.code) metadata.entityName else return,
                artistName = metadata.trackMetadata?.artistName
                    ?: if (metadata.entityType == ReviewEntityType.ARTIST.code) metadata.entityName else null,
                releaseName = metadata.trackMetadata?.releaseName
                    ?: if (metadata.entityType == ReviewEntityType.RELEASE_GROUP.code) metadata.entityName else null,
                onDismiss = dialogsState::deactivateDialog,
                isCritiqueBrainzLinked = viewModel::isCritiqueBrainzLinked,
                onSubmit = { type, blurbContent, rating, locale ->
                    viewModel.review(
                        metadata = metadata,
                        entityType = type,
                        blurbContent = blurbContent,
                        rating = rating,
                        locale = locale
                    )
                }
            )
        }
    }

    SelectPlaylist(
        trackMetadata = metadata,
        isSheetVisible = currentSheet == SocialDropdownSheets.SELECT_PLAYLIST_TO_ADD_TRACK,
        onCreateNewPlaylist = {
            currentSheet = SocialDropdownSheets.CREATE_NEW_PLAYLIST
        },
        onDismiss = {
            currentSheet = SocialDropdownSheets.NONE
        }
    )
    CreateEditPlaylistScreen(
        onDismiss = {
            currentSheet = SocialDropdownSheets.SELECT_PLAYLIST_TO_ADD_TRACK
        },
        isVisible = currentSheet == SocialDropdownSheets.CREATE_NEW_PLAYLIST,
        mbid = null
    )

}


/** This layer tries to define what options in the dropdown menu are to be shown.*/
@Composable
fun SocialDropdown(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    metadata: Metadata,
    onOpenInMusicBrainz: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    onRecommend: (() -> Unit)? = null,
    onPersonallyRecommend: (() -> Unit)? = null,
    onReview: (() -> Unit)? = null,
    onAddToPlaylist: (() -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,

    // TODO: Implement these
    onLink: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onInspect: (() -> Unit)? = null
) {
    val list = remember {
        val trackName = metadata.trackMetadata?.trackName
            ?: if (metadata.entityType == ReviewEntityType.RECORDING.code) metadata.entityName else null
        val artistName = metadata.trackMetadata?.artistName
            ?: if (metadata.entityType == ReviewEntityType.ARTIST.code) metadata.entityName else null
        val releaseName = metadata.trackMetadata?.releaseName
            ?: if (metadata.entityType == ReviewEntityType.RELEASE_GROUP.code) metadata.entityName else null
        val recordingMbid = metadata.trackMetadata?.mbidMapping?.recordingMbid
            ?: metadata.reviewMbid
            ?: metadata.trackMetadata?.additionalInfo?.recordingMbid

        mutableListOf<SocialDropdownItem>().apply {
            if (recordingMbid != null) {
                add(SocialDropdownItem.OPEN_IN_MUSICBRAINZ(onOpenInMusicBrainz))
            }

            if (trackName != null && artistName != null) {
                add(SocialDropdownItem.PIN(onPin))

                // Mbid or msid
                if (recordingMbid != null || metadata.trackMetadata?.additionalInfo?.recordingMsid != null) {
                    add(SocialDropdownItem.RECOMMEND(onRecommend))
                    add(SocialDropdownItem.PERSONALLY_RECOMMEND(onPersonallyRecommend))
                }
            }

            if (trackName != null || artistName != null || releaseName != null)
                add(SocialDropdownItem.REVIEW(onReview))

            if (recordingMbid != null && trackName != null)
                add(SocialDropdownItem.ADD_TO_PLAYLIST(onAddToPlaylist))

            if(onRemoveFromPlaylist != null)
                add(SocialDropdownItem.REMOVE_FROM_PLAYLIST(onRemoveFromPlaylist))

            // TODO: Add these in future once we have its metadata conditions.
            //add(SocialDropdownItem.LINK(onLink))
            //add(SocialDropdownItem.DELETE(onDelete)),
            //add(SocialDropdownItem.INSPECT(onInspect))
        }
    }

    SocialDropdown(
        isExpanded = isExpanded,
        onDismiss = onDismiss,
        itemList = list
    )
}

@Composable
private fun SocialDropdown(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    itemList: List<SocialDropdownItem>
) {
    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss) {
        itemList.forEach { item ->
            DropdownItem(
                icon = item.icon,
                title = item.title,
                onClick = item.onClick
                    ?: return@forEach   // We are sure that we would get the same result everytime.
            )
        }
    }
}


@Composable
private fun DropdownItem(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                modifier = Modifier.size(ListenBrainzTheme.sizes.dropdownItem),
                painter = painterResource(id = icon),
                contentDescription = title
            )
        },
        text = {
            Text(
                text = title,
                style = ListenBrainzTheme.textStyles.dropdownItem
            )
        },
        onClick = onClick
    )
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SocialDropDownPreview() {
    ListenBrainzTheme {
        Surface {
            Box {
                Surface(modifier = Modifier.size(20.dp)) {}
                SocialDropdown(
                    isExpanded = true,
                    onDismiss = {},
                    itemList = listOf(
                        SocialDropdownItem.OPEN_IN_MUSICBRAINZ {},
                        SocialDropdownItem.PIN {},
                        SocialDropdownItem.RECOMMEND {},
                        SocialDropdownItem.PERSONALLY_RECOMMEND {},
                        SocialDropdownItem.LINK {},
                        SocialDropdownItem.REVIEW {},
                        SocialDropdownItem.DELETE {},
                        SocialDropdownItem.ADD_TO_PLAYLIST {},
                        SocialDropdownItem.INSPECT {},
                        SocialDropdownItem.REMOVE_FROM_PLAYLIST {},
                    )
                )
            }
        }
    }
}