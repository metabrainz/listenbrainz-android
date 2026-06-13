package org.listenbrainz.android.util

import org.listenbrainz.android.R
import org.listenbrainz.shared.util.IconKeys

fun getLinkCardRes(iconKey:String):Int{
    return when(iconKey){
        IconKeys.WIKIDATA -> R.drawable.wiki_data
        IconKeys.LYRICS -> R.drawable.settings_voice
        IconKeys.HOMEPAGE -> R.drawable.home_icon
        IconKeys.PURCHASE -> R.drawable.mail_order
        IconKeys.MAIL_ORDER -> R.drawable.mail_order
        IconKeys.MUSICBRAINZ_LOGO -> R.drawable.musicbrainz_logo
        else -> 0
    }
}