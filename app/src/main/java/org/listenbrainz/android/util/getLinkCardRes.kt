package org.listenbrainz.android.util

import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.home_icon
import org.listenbrainz.shared.resources.mail_order
import org.listenbrainz.shared.resources.musicbrainz_logo
import org.listenbrainz.shared.resources.settings_voice
import org.listenbrainz.shared.resources.wiki_data
import org.listenbrainz.shared.util.IconKeys

fun getLinkCardRes(iconKey:String): DrawableResource? {
    return when(iconKey){
        IconKeys.WIKIDATA -> Res.drawable.wiki_data
        IconKeys.LYRICS -> Res.drawable.settings_voice
        IconKeys.HOMEPAGE -> Res.drawable.home_icon
        IconKeys.PURCHASE -> Res.drawable.mail_order
        IconKeys.MAIL_ORDER -> Res.drawable.mail_order
        IconKeys.MUSICBRAINZ_LOGO -> Res.drawable.musicbrainz_logo
        else -> null
    }
}