package org.listenbrainz.android.model

import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.feed.FeedListenArtist

@Serializable
data class Song (
    val mediaID : Long=0L,
    val title : String="",
    val trackNumber : Int=0,
    val year : Int=0,
    val duration : Long=0L,
    val dateModified : Long=0L,
    val artistId : Long=0L,
    val artist: String="",
    val uri : String="",
    val albumID: Long=0L,
    val album: String="",
    val albumArt: String="",
    val discNumber : Long = 0L,
    var lastListenedTo : Long = 0L
) {
    fun toMetadata(): Metadata = Metadata(
        trackMetadata = TrackMetadata(
            artistName = artist,
            releaseName = album,
            trackName = title,
            mbidMapping = MbidMapping(
                artistMbids = emptyList(),
                recordingName = title,
                artists = listOf(FeedListenArtist(artist, null, null))
            ),
            additionalInfo = null
        )
    )

    /** @return whether this track is nothing or not.
     * @see [org.listenbrainz.android.service.NOTHING_PLAYING]
     */
    fun isNothing() = mediaID == 0L && duration == 0L

    companion object {
        val emptySong = Song(
            mediaID = 0L,
            title = "",
            trackNumber = 0,
            year = 0,
            duration = 0L,
            dateModified = 0L,
            artistId = 0L,
            artist = "",
            uri = "",
            albumID = 0L,
            album = "",
            albumArt = "",
            discNumber = 0L,
            lastListenedTo = 0L
        )
        
        fun preview(): Song =
            Song(
                mediaID = 0L,
                title = "Title",
                trackNumber = 0,
                year = 2024,
                duration = 30000L,
                dateModified = 0L,
                artistId = 0L,
                artist = "Artist",
                uri = "",
                albumID = 0L,
                album = "Album",
                albumArt = "",
                discNumber = 0L,
                lastListenedTo = 0L
            )
    }
}
