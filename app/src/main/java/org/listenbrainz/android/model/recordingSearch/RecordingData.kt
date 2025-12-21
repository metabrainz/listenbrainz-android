package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.listenbrainz.android.model.playlist.AdditionalMetadataTrack
import org.listenbrainz.android.model.playlist.PlaylistArtist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.playlist.TrackExtension
import org.listenbrainz.android.model.playlist.TrackExtensionData

@Serializable
data class RecordingData(
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit> = emptyList(),
    @SerialName("first-release-date")
    val firstReleaseDate: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("length")
    val length: Int? = null,
    @SerialName("releases")
    val releases: List<RecordingRelease> = emptyList(),
    @SerialName("score")
    val score: Int? = null,
    @SerialName("tags")
    val tags: List<RecordingTag> = emptyList(),
    @SerialName("title")
    val title: String? = null,
    @SerialName("video")
    val video: JsonElement? = null
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