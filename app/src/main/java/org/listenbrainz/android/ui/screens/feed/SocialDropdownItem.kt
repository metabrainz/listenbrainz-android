@file:Suppress("ClassName")

package org.listenbrainz.android.ui.screens.feed

import org.listenbrainz.android.R

/** A class use to feed the dropdowns with data using singular format for consistency across app.*/
interface SocialDropdownItem {
    val icon: Int
    val title: String
    val onClick: (() -> Unit)?
    
    class OPEN_IN_MUSICBRAINZ(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_redirect
        override val title: String = "Open in MusicBrainz"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class PIN(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_pin
        override val title: String = "Pin this track"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class RECOMMEND(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_recommend
        override val title: String = "Recommend to my followers"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class PERSONALLY_RECOMMEND(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_send
        override val title: String = "Personally recommend"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class LINK(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_link
        override val title: String = "Link with MusicBrainz"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class REVIEW(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_review
        override val title: String = "Write a review"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class DELETE(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_delete
        override val title: String = "Delete listen"
        override val onClick: (() -> Unit)? = onClick
    }
    
    class INSPECT(onClick: (() -> Unit)? = null): SocialDropdownItem {
        override val icon: Int = R.drawable.ic_code
        override val title: String = "Inspect listen"
        override val onClick: (() -> Unit)? = onClick
    }
}