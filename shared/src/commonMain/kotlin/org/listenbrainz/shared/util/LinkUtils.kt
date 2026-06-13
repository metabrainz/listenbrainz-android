package org.listenbrainz.shared.util


import org.listenbrainz.shared.model.artist.Rels
import org.listenbrainz.shared.util.Constants.MB_BASE_URL


enum class ArtistLinksEnum(val label: String) {
    ALL("All"),
    MAIN("Main"),
    STREAMING("Streaming"),
    SOCIAL_MEDIA("Social Media"),
    LYRICS("Lyrics")
}

object IconKeys {
    const val WIKIDATA = "wikidata"
    const val LYRICS = "lyrics"
    const val HOMEPAGE = "homepage"
    const val PURCHASE = "purchase"
    const val MAIL_ORDER = "mail_order"
    const val MUSICBRAINZ_LOGO = "musicbrainz_logo"
}

class LinkCardData(
    val iconKey: String,
    val label: String,
    val url: String
)

object LinkUtils {
    fun parseLinks(artistMbid: String?, links: Rels?): Map<ArtistLinksEnum, List<LinkCardData>> {
        val allLinkCards: MutableList<LinkCardData> = mutableListOf()
        val mainLinkCards: MutableList<LinkCardData> = mutableListOf()
        val streamingLinkCards: MutableList<LinkCardData> = mutableListOf()
        val socialMediaLinkCards: MutableList<LinkCardData> = mutableListOf()
        val lyricsLinkCards: MutableList<LinkCardData> = mutableListOf()

        links?.let { it ->
            it.wikidata?.let { url ->
                val card = LinkCardData(IconKeys.WIKIDATA, "Wikidata", url)
                allLinkCards.add(card)
                mainLinkCards.add(card)
            }
            it.lyrics?.let { url ->
                val card = LinkCardData(IconKeys.LYRICS, "Lyrics", url)
                allLinkCards.add(card)
                lyricsLinkCards.add(card)
            }
            it.officialHomePage?.let { url ->
                val card = LinkCardData(IconKeys.HOMEPAGE, "Homepage", url)
                allLinkCards.add(card)
                mainLinkCards.add(card)
            }
            it.purchaseForDownload?.let { url ->
                val card = LinkCardData(IconKeys.PURCHASE, "Purchase for Download", url)
                allLinkCards.add(card)
                streamingLinkCards.add(card)
            }
            it.purchaseForMailOrder?.let { url ->
                val card = LinkCardData(IconKeys.MAIL_ORDER, "Purchase for mail order", url)
                allLinkCards.add(card)
                streamingLinkCards.add(card)
            }
        }
        if (artistMbid != null) {
            val musicBrainzCard = LinkCardData(
                IconKeys.MUSICBRAINZ_LOGO,
                "Edit",
                "${MB_BASE_URL}artist/$artistMbid"
            )
            allLinkCards.add(musicBrainzCard)
            mainLinkCards.add(musicBrainzCard)
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