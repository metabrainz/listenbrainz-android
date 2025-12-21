package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AdditionalInfo(
    @SerialName("artist_msid")
    val artistMsid: String? = null,
    @SerialName("artist_names")
    val artistNames: List<String>? = null,
    @SerialName("discnumber")
    val discNumber: Int? = null,
    @SerialName("duration_ms")
    val durationMs: Int? = null,
    @SerialName("isrc")
    val isrc: String? = null,
    @SerialName("listening_from")
    val listeningFrom: String? = null,
    @SerialName("recording_msid")
    val recordingMsid: String? = null,
    @SerialName("release_artist_name")
    val releaseArtistName: String? = null,
    @SerialName("release_artist_names")
    val releaseArtistNames: List<String>? = null,
    @SerialName("release_msid")
    val releaseMsid: String? = null,
    @SerialName("release_mbid")
    val releaseMbid: String? = null,
    @SerialName("spotify_album_artist_ids")
    val spotifyAlbumArtistIds: List<String>? = null,
    @SerialName("spotify_album_id")
    val spotifyAlbumId: String? = null,
    @SerialName("spotify_artist_ids")
    val spotifyArtistIds: List<String>? = null,
    @SerialName("spotify_id")
    val spotifyId: String? = null,
    @SerialName("tracknumber")
    val trackNumber: Int? = null,

    // Used for listen submission body
    @SerialName("media_player")
    val mediaPlayer: String? = null,
    @SerialName("submission_client")
    val submissionClient: String? = null,
    @SerialName("submission_client_version")
    val submissionClientVersion: String? = null,

    // Feed Specific
    @SerialName("artist_mbids")
    val artistMbids: List<String>? = null,
    @SerialName("origin_url")
    val originUrl: String? = null,
    @SerialName("recording_mbid")
    val recordingMbid: String? = null,
    @SerialName("release_group_mbid")
    val releaseGroupMbid: String? = null,
)