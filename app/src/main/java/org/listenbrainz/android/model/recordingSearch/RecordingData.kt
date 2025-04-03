package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.playlist.AdditionalMetadataTrack
import org.listenbrainz.android.model.playlist.PlaylistArtist
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.playlist.TrackExtension
import org.listenbrainz.android.model.playlist.TrackExtensionData

data class RecordingData(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit> = emptyList(),
    @SerializedName("first-release-date")
    val firstReleaseDate: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("length")
    val length: Int? = null,
    @SerializedName("releases")
    val releases: List<RecordingRelease> = emptyList(),
    @SerializedName("score")
    val score: Int? = null,
    @SerializedName("tags")
    val tags: List<RecordingTag> = emptyList(),
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("video")
    val video: Any? = null
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