package org.listenbrainz.android.ui.screens.feed

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

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
            if (recordingMbid != null)
                add(SocialDropdownItem.OPEN_IN_MUSICBRAINZ(onOpenInMusicBrainz))
        
            if (trackName != null && artistName != null){
                add(SocialDropdownItem.PIN(onPin))
                
                // Mbid or msid
                if (recordingMbid != null || metadata.trackMetadata?.additionalInfo?.recordingMsid != null) {
                    add(SocialDropdownItem.RECOMMEND(onRecommend))
                    add(SocialDropdownItem.PERSONALLY_RECOMMEND(onPersonallyRecommend))
                }
            }
            
            if (trackName != null || artistName != null || releaseName != null)
                add(SocialDropdownItem.REVIEW(onReview))
            
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
){
    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss) {
        itemList.forEach { item ->
            DropdownItem(
                icon = item.icon,
                title = item.title,
                onClick = item.onClick ?: return@forEach   // We are sure that we would get the same result everytime.
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
fun SocialDropDownPreview(){
    ListenBrainzTheme {
        Surface {
            Box {
                Surface(modifier = Modifier.size(20.dp)){}
                SocialDropdown(
                    isExpanded = true,
                    onDismiss = {},
                    itemList = listOf(
                        SocialDropdownItem.OPEN_IN_MUSICBRAINZ {},
                        SocialDropdownItem.PIN{},
                        SocialDropdownItem.RECOMMEND{},
                        SocialDropdownItem.PERSONALLY_RECOMMEND{},
                        SocialDropdownItem.LINK{},
                        SocialDropdownItem.REVIEW{},
                        SocialDropdownItem.DELETE{},
                        SocialDropdownItem.INSPECT{}
                    )
                )
            }
        }
    }
}