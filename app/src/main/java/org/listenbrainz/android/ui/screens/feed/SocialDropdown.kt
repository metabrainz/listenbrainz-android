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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun FeedSocialDropdown(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onOpenInMusicBrainz: () -> Unit = {},
    onPin: () -> Unit = {},
    onRecommend: () -> Unit = {},
    onPersonallyRecommend: () -> Unit = {},
    // TODO: Implement rest of the functionalities.
    //onLink: () -> Unit,
    onReview: () -> Unit = {},
    //onDelete: () -> Unit,
    //onInspect: () -> Unit
) {
    val list = listOf(
        SocialDropdownItem(
            icon = R.drawable.ic_redirect,
            title = "Open in MusicBrainz",
            onClick = onOpenInMusicBrainz
        ),
        SocialDropdownItem(
            icon = R.drawable.ic_pin,
            title = "Pin this track",
            onClick = onPin
        ),
        SocialDropdownItem(
            icon = R.drawable.ic_recommend,
            title = "Recommend to my followers",
            onClick = onRecommend
        ),
        SocialDropdownItem(
            icon = R.drawable.ic_send,
            title = "Personally recommend",
            onClick = onPersonallyRecommend
        ),
        /*SocialDropdownItem(
            icon = R.drawable.ic_link,
            title = "Link with MusicBrainz",
            onClick = onLink
        ),*/
        SocialDropdownItem(
            icon = R.drawable.ic_review,
            title = "Write a review",
            onClick = onReview
        ),
        /*SocialDropdownItem(
            icon = R.drawable.ic_delete,
            title = "Delete listen",
            onClick = onDelete
        ),
        SocialDropdownItem(
            icon = R.drawable.ic_code,
            title = "Inspect listen",
            onClick = onInspect
        )*/
    )
    
    SocialDropdown(
        isExpanded = isExpanded,
        onDismiss = onDismiss,
        itemList = list
    )
}

@Composable
fun SocialDropdown(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    itemList: List<SocialDropdownItem>
){
    DropdownMenu(expanded = isExpanded, onDismissRequest = onDismiss) {
        itemList.forEach { item ->
            DropdownItem(
                icon = item.icon,
                title = item.title,
                onClick = item.onClick
            )
        }
    }
}

data class SocialDropdownItem(
    val icon: Int,
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun DropdownItem(
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
                SocialDropdown(
                    isExpanded = true,
                    onDismiss = {},
                    itemList = listOf(),
                    /*onOpenInMusicBrainz = {},
                    onPin = {},
                    onRecommend = {},
                    onPersonallyRecommend = {},
                    onLink = {},
                    onReview = {},
                    onDelete = {},
                    onInspect = {}*/
                )
            }
        }
    }
}