@file:Suppress("ClassName")

package org.listenbrainz.android.ui.screens.feed

import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.add_track_to_playlist
import org.listenbrainz.shared.resources.ic_code
import org.listenbrainz.shared.resources.ic_delete
import org.listenbrainz.shared.resources.ic_link
import org.listenbrainz.shared.resources.ic_pin
import org.listenbrainz.shared.resources.ic_recommend
import org.listenbrainz.shared.resources.ic_redirect
import org.listenbrainz.shared.resources.ic_review
import org.listenbrainz.shared.resources.ic_send
import org.listenbrainz.shared.resources.playlist_remove_circle

/** A class use to feed the dropdowns with data using singular format for consistency across app.
 * @property onClick This is a nullable function used to determine if the if we have to show the option in the first place.*/
interface SocialDropdownItem {
    val icon: DrawableResource
    val title: String
    val onClick: (() -> Unit)?
    
    class OPEN_IN_MUSICBRAINZ(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_redirect
        override val title: String = "Open in MusicBrainz"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class PIN(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_pin
        override val title: String = "Pin this track"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class RECOMMEND(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_recommend
        override val title: String = "Recommend to my followers"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class PERSONALLY_RECOMMEND(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_send
        override val title: String = "Personally recommend"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class LINK(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_link
        override val title: String = "Link with MusicBrainz"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class REVIEW(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_review
        override val title: String = "Write a review"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class DELETE(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_delete
        override val title: String = "Delete listen"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class INSPECT(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.ic_code
        override val title: String = "Inspect listen"
        override val onClick: (() -> Unit)? = onClick
    }

    class ADD_TO_PLAYLIST(onClick: (() -> Unit)?): SocialDropdownItem {
        override val icon: DrawableResource = Res.drawable.add_track_to_playlist
        override val title: String = "Add to playlist"
        override val onClick: (() -> Unit)? = onClick
    }

    class REMOVE_FROM_PLAYLIST(onClick: (() -> Unit)?): SocialDropdownItem{
        override val icon: DrawableResource = Res.drawable.playlist_remove_circle
        override val title: String = "Remove from playlist"
        override val onClick: (() -> Unit)? = onClick
    }
}