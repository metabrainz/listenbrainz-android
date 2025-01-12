package org.listenbrainz.android.util


import ArtistLinksEnum
import org.listenbrainz.android.R
import org.listenbrainz.android.model.artist.Rels
import org.listenbrainz.android.ui.screens.artist.LinkCardData
import org.listenbrainz.android.util.Constants.MB_BASE_URL

object LinkUtils {
    fun parseLinks(artistMbid: String?, links: Rels?): Map<ArtistLinksEnum, List<LinkCardData>> {
        val allLinkCards: MutableList<LinkCardData> = mutableListOf()
        val mainLinkCards: MutableList<LinkCardData> = mutableListOf()
        val streamingLinkCards: MutableList<LinkCardData> = mutableListOf()
        val socialMediaLinkCards: MutableList<LinkCardData> = mutableListOf()
        val lyricsLinkCards: MutableList<LinkCardData> = mutableListOf()

        if (links?.wikidata != null) {
            val wikidata = LinkCardData(
                R.drawable.wiki_data,
                "Wikidata",
                links.wikidata
            )
            allLinkCards.add(wikidata)
            mainLinkCards.add(wikidata)
        }
        if (links?.lyrics != null) {
            val lyrics = LinkCardData(R.drawable.settings_voice, "Lyrics", links.lyrics)
            allLinkCards.add(lyrics)
            lyricsLinkCards.add(lyrics)
        }
        if (links?.officialHomePage != null) {
            val homePage = LinkCardData(
                R.drawable.home_icon,
                "Homepage",
                links.officialHomePage
            )
            allLinkCards.add(homePage)
            mainLinkCards.add(homePage)
        }
        if (links?.purchaseForDownload != null) {
            val purchase = LinkCardData(
                R.drawable.mail_order,
                "Purchase for Download",
                links.purchaseForDownload
            )
            allLinkCards.add(purchase)
            streamingLinkCards.add(purchase)
        }
        if (links?.purchaseForMailOrder != null) {
            val mailOrder = LinkCardData(
                R.drawable.mail_order,
                "Purchase for mail order",
                links.purchaseForMailOrder
            )
            allLinkCards.add(mailOrder)
            streamingLinkCards.add(mailOrder)
        }
        if (artistMbid != null) {
            mainLinkCards.add(
                LinkCardData(
                    R.drawable.musicbrainz_logo,
                    "Edit",
                    MB_BASE_URL + "artist/${artistMbid}"
                )
            )
        }

        return mapOf(
            ArtistLinksEnum.ALL to allLinkCards,
            ArtistLinksEnum.MAIN to mainLinkCards,
            ArtistLinksEnum.LYRICS to lyricsLinkCards,
            ArtistLinksEnum.STREAMING to streamingLinkCards,
            ArtistLinksEnum.SOCIAL_MEDIA to socialMediaLinkCards
        )
    }
}