package org.listenbrainz.android.model

object LinksClassifier {
    private const val OFFICIAL_HOMEPAGE = "official homepage"
    const val DISCOGRAPHY = "discography"
    private const val FANPAGE = "fanpage"
    private const val BIOGRAPHY = "biography"
    const val BBC_MUSIC_PAGE = "BBC Music Page"
    const val INTERVIEW = "interview"
    private const val IMAGE = "image"
    const val LYRICS = "lyrics"
    const val ONLINE_DATA = "online_data"
    const val SOCIAL_NETWORK = "social network"
    const val MYSPACE = "myspace"
    const val PUREVOLUME = "purevolume"
    const val SOUNDCLOUD = "soundcloud"
    const val VIDEO_CHANNEL = "video channel"
    const val YOUTUBE = "youtube"
    const val ONLINE_COMMUNITY = "online community"
    const val BLOG = "blog"
    const val CROWDFUNDING = "crowdfunding"
    const val PATRONAGE = "patronage"
    private const val PURCHASE_FOR_MAIL_ORDER = "purchase for mail-order"
    private const val PURCHASE_FOR_DOWNLOAD = "purchase for download"
    private const val DOWNLOAD_FOR_FREE = "download for free"
    private const val STREAMING_MUSIC = "streaming music"
    const val BANDCAMP = "bandcamp"
    const val OTHER_DATABASES = "other databases"
    const val ALLMUSIC = "allmusic"
    const val BOOKBRAINZ = "bookbrainz"
    const val DISCOGS = "discogs"
    const val IMDB = "IMDb"
    const val IMSLP = "IMSLP"
    private const val LAST_FM = "last.fm"
    const val SECOND_HAND_SONGS = "secondhandsongs"
    const val SET_LIST_FM = "setlistfm"
    const val SONGKICK = "songkick"
    const val VGMDB = "vgmdb"
    const val VIAF = "VIAF"
    private const val WIKIDATA = "wikidata"
    private const val WIKIPEDIA = "wikipedia"
    private const val INFO = "info"
    private const val MISC = "misc"
    private const val GET_THE_MUSIC = "get the music"
    fun classifyToDisplayText(link: Link): String {
        val type = link.type
        val url = link.url!!.resource
        return if (type != null && !type.isEmpty() && url != null && !url.isEmpty()) {
            when (type) {
                STREAMING_MUSIC -> "<a href=\"$url\"> Stream Music </a>"
                PURCHASE_FOR_DOWNLOAD -> "&lt;a href=http://www.google.co.in&gt;Google&lt;/a&gt;"
                PURCHASE_FOR_MAIL_ORDER -> "<a href=\"$url\"> Purchase for mail-order </a>"
                DOWNLOAD_FOR_FREE -> "<a href=\"$url\"> Download for free </a>"
                YOUTUBE -> "<a href=\"$url\"> Stream Music </a>"
                GET_THE_MUSIC -> "<a href=\"$url\"> Get the Music </a>"
                IMAGE, BIOGRAPHY, WIKIDATA, WIKIPEDIA, OFFICIAL_HOMEPAGE, FANPAGE, LAST_FM, INFO -> url
                else -> url
            }
        } else MISC
    }

    fun classifyCategory(link: Link): String {
        val type = link.type
        return if (type != null && !type.isEmpty()) {
            when (type) {
                STREAMING_MUSIC, PURCHASE_FOR_DOWNLOAD, PURCHASE_FOR_MAIL_ORDER, DOWNLOAD_FOR_FREE, YOUTUBE, GET_THE_MUSIC -> GET_THE_MUSIC
                IMAGE, BIOGRAPHY, WIKIDATA, WIKIPEDIA, OFFICIAL_HOMEPAGE, FANPAGE, LAST_FM, INFO -> INFO
                else -> MISC
            }
        } else MISC
    }
}