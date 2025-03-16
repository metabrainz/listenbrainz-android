package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.playlist.AdditionalMetadataTrack
import org.listenbrainz.android.model.playlist.PlaylistArtist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.playlist.TrackExtension
import org.listenbrainz.android.model.playlist.TrackExtensionData

data class RecordingData(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>?,
    @SerializedName("first-release-date")
    val firstReleaseDate: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("releases")
    val releases: List<RecordingRelease>?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("tags")
    val tags: List<RecordingTag>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("video")
    val video: Any?
){
    fun toPlaylistTrack(): PlaylistTrack{
        val artistIdentifier: MutableList<String> = mutableListOf()
        artistCredit?.forEach { artistCredit ->
            if(!artistCredit.artist?.id.isNullOrEmpty())
            artistIdentifier.add("https://musicbrainz.org/artist/${artistCredit.artist?.id}")
        }

        return PlaylistTrack(
            album = releases?.getOrNull(0)?.title,
            creator = artistCredit?.getOrNull(0)?.name,
            duration = length,
            extension = TrackExtension(
                trackExtensionData = TrackExtensionData(
                    artistIdentifiers = artistIdentifier,
                    addedAt = firstReleaseDate,
                    additionalMetadata = AdditionalMetadataTrack(
                        artists = artistCredit?.map {
                            PlaylistArtist(
                                artistCreditName = it.artist?.name,
                                artistMbid = it.artist?.id,
                                joinPhrase = it.joinphrase
                            )
                        }?: emptyList(),
                        caaReleaseMbid = releases?.getOrNull(0)?.id
                    )
                )
            ),
            identifier = if(id.isNullOrEmpty()) emptyList() else listOf(
                "https://musicbrainz.org/recording/$id"
            ),
            title = title
        )
    }
}