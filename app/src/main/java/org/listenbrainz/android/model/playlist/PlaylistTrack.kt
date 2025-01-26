package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist

data class PlaylistTrack(
    @SerializedName("album")
    val album: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("extension")
    val extension: TrackExtension = TrackExtension(),
    @SerializedName("identifier")
    val identifier: List<String> = listOf(),
    @SerializedName("title")
    val title: String? = null
){
    fun toMetadata(): Metadata{
        val artistMBID = extension.trackExtensionData.additionalMetadata.artists.map{it.artistMbid?:""}
        val artist = extension.trackExtensionData.additionalMetadata.artists
        val data = Metadata(
            trackMetadata = TrackMetadata(
                artistName = creator?:"",
                releaseName = album,
                trackName = title?:"",
                mbidMapping = MbidMapping(
                    artistMbids = artistMBID,
                    artists = artist.map {
                        FeedListenArtist(
                            it.artistCreditName?:"",
                            it.artistMbid,
                            it.joinPhrase
                        )
                    },
                    recordingName = title?:""
                ),
                additionalInfo = AdditionalInfo(
                    artistMbids = artistMBID,
                    artistNames = artist.map { it.artistCreditName?:"" },
                    durationMs = duration?:0,
                    recordingMbid = getRecordingMBID(),
                )
            )
        )
        return data
    }

    fun getRecordingMBID(): String?{
        val url = identifier.firstOrNull()
        val regex = """recording/([a-f0-9\-]+)""".toRegex()
        val matchResult = url?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }
}